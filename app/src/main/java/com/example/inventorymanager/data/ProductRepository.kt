package com.example.inventorymanager.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

class ProductRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val productsCollection = firestore.collection("inventory")

    fun getProducts(): Flow<List<InventoryItem>> = callbackFlow {
        val listener = productsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val itemsWithId = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(InventoryItem::class.java)?.copy(id = doc.id)
                }
                trySend(itemsWithId)
            }
        }
        awaitClose { listener.remove() }
    }

    fun getDashboardStats(): Flow<DashboardStats?> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val docRef = firestore.collection("users").document(userId)
            .collection("stats").document("dashboard")

        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(snapshot.toObject(DashboardStats::class.java))
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun addProduct(item: InventoryItem): Result<String> {
        return try {
            val data = hashMapOf(
                "name" to item.name,
                "quantity" to item.quantity,
                "price" to item.price,
                "category" to item.category,
                "imageUri" to item.imageUri,
                "totalSold" to item.totalSold,
                "lastUpdated" to FieldValue.serverTimestamp()
            )
            val docRef = productsCollection.add(data).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun updateProduct(item: InventoryItem): Result<Unit> {
        return try {
            productsCollection.document(item.id).set(item).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveReconciliation(totalRevenue: Double, totalEmpties: Int, updatedItems: List<InventoryItem>): Result<Unit> {
        val userId = auth.currentUser?.uid
            ?: return Result.failure(Exception("User not authenticated"))

        return try {
            val batch = firestore.batch()
            
            val historyRef = firestore.collection("users").document(userId)
                .collection("reconciliations").document()
            val historyData = ReconciliationRecord(
                totalRevenue = totalRevenue,
                totalEmpties = totalEmpties
            )
            batch.set(historyRef, historyData)
            
            val statsRef = firestore.collection("users").document(userId)
                .collection("stats").document("dashboard")
            val statsUpdates = hashMapOf(
                "totalRevenue" to FieldValue.increment(totalRevenue),
                "totalEmpties" to FieldValue.increment(totalEmpties.toLong())
            )
            batch.set(statsRef, statsUpdates, SetOptions.merge())
            
            updatedItems.forEach { item ->
                val productRef = productsCollection.document(item.id)
                batch.set(productRef, item)
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun receiveDelivery(deliveryMap: Map<String, Int>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            deliveryMap.forEach { (itemId, receivedQuantity) ->
                val docRef = productsCollection.document(itemId)
                batch.update(docRef, "quantity", FieldValue.increment(receivedQuantity.toLong()))
                batch.update(docRef, "lastUpdated", FieldValue.serverTimestamp())
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadImage(localPath: String): Result<String> {
        return try {
            val file = Uri.fromFile(File(localPath))
            val fileName = "images/${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child(fileName)
            
            ref.putFile(file).await()
            val downloadUrl = ref.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(id: String): Result<Unit> {
        return try {
            productsCollection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
