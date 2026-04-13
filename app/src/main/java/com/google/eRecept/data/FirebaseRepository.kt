package com.google.eRecept.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    init {
        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
            .build()
        db.firestoreSettings = settings
    }

    val currentUserId: String? get() = auth.currentUser?.uid

    fun getAppointments(doctorId: String): Flow<List<Appointment>> {
        return db.collection("appointments")
            .whereEqualTo("doctor_id", doctorId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Appointment::class.java)
            }
    }

    fun getRecentRecipes(doctorId: String): Flow<List<Recipe>> {
        return db.collection("recipes")
            .whereEqualTo("doctor_id", doctorId)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(10)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Recipe::class.java)
            }
    }

    suspend fun createAppointment(appointment: Appointment) {
        db.collection("appointments").add(appointment).await()
    }

    suspend fun createRecipe(recipe: Recipe): String {
        val docRef = db.collection("recipes").add(recipe).await()
        return docRef.id
    }

    suspend fun getPatientByIin(iin: String): Patient? {
        return try {
            val doc = db.collection("patients").document(iin).get().await()
            if (doc.exists()) {
                doc.toObject(Patient::class.java)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun searchPatients(query: String): List<Patient> {
        if (query.length == 12 && query.all { it.isDigit() }) {
            val p = getPatientByIin(query)
            return if (p != null) listOf(p) else emptyList()
        }
        return try {
            db.collection("patients")
                .orderBy("full_name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(20)
                .get()
                .await()
                .toObjects(Patient::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchMedications(query: String): List<Medication> {
        return try {
            db.collection("medications")
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(20)
                .get()
                .await()
                .toObjects(Medication::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDoctorProfile(doctorId: String): Doctor? {
        return try {
            val doc = db.collection("doctors").document(doctorId).get().await()
            doc.toObject(Doctor::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun logout() {
        auth.signOut()
    }
}
