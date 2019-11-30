/*
 * Copyright 2019 Oliver Rhyme G. Añasco
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.snorlax.snorlax.data.firebase

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import com.snorlax.snorlax.model.Attendance
import com.snorlax.snorlax.model.Student
import com.snorlax.snorlax.model.User
import com.snorlax.snorlax.utils.getTodayDate
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*

class FirebaseFirestoreSource private constructor() {

    private val firestoreDB = FirebaseFirestore.getInstance()

    private val userRef = firestoreDB.collection(USER_DATA_NAME)
    private val sectionRef = firestoreDB.collection(SECTIONS_DATA_NAME)
//    private val attendanceRef = sectionRef.collection(ATTENDANCE_DATA_NAME)

//    private val grade11SectionQuery = sectionRef.whereEqualTo("grade_level", 11)
//    private val grade12SectionQuery = sectionRef.whereEqualTo("grade_level", 12)
//
//    val studentGroup = firestoreDB.collectionGroup(STUDENTS_DATA_NAME)
//    val attendanceGroup = firestoreDB.collectionGroup(ATTENDANCE_DATA_NAME)


    companion object {

        private var instance: FirebaseFirestoreSource? = null

        fun getInstance(): FirebaseFirestoreSource {
            instance?.let {
                return it
            }

            instance = FirebaseFirestoreSource()
            return getInstance()
        }

        private const val STUDENTS_DATA_NAME = "student"
        private const val SECTIONS_DATA_NAME = "section"
        private const val ATTENDANCE_DATA_NAME = "attendance"
        private const val USER_DATA_NAME = "user"

    }
    fun getDocumentReference(section: String, lrn: String): DocumentReference {
        return sectionRef
            .document(section)
            .collection(STUDENTS_DATA_NAME)
            .document(lrn)
    }

    fun getAdmin(uid: String): Single<User> {
        return Single.create<User> { emitter ->
            userRef
                .document(uid)
                .get()
                .addOnSuccessListener {
                    emitter.onSuccess(it.toObject(User::class.java)!!)
                }
                .addOnFailureListener {
                    emitter.onError(it)
                }
        }.subscribeOn(Schedulers.io())
    }

    fun addAdmin(user: User): Completable {
        return Completable.create { emitter ->
            userRef
                .document(user.uid)
                .set(user)
                .addOnCompleteListener {
                    if (it.isSuccessful) emitter.onComplete()
                    else emitter.onError(it.exception!!)
                }
        }.subscribeOn(Schedulers.io())
    }

    fun getStudentList(section: String): Single<List<Student>> {
        return Single.create { emitter ->
            Log.d("Threading", "get student list ${Thread.currentThread().name}")
            sectionRef
                .document(section)
                .collection(STUDENTS_DATA_NAME)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    firebaseFirestoreException?.let {
                        emitter.onError(it)
                        return@addSnapshotListener
                    }
                    val students = arrayListOf<Student>()

                    for (document in querySnapshot!!) {
                        students.add(document.toObject(Student::class.java))
                    }
                    if (students.isEmpty()) emitter.onSuccess(listOf())
                    else {
                        students.sortBy {
                            it.name.getValue("last")[0]
                        }
                        emitter.onSuccess(students)
                    }
                }
        }
    }


    fun getStudentQuery(section: String): Single<Query> {
        return Single.create<Query> { emitter ->
            val listener = sectionRef
                .document(section)
                .collection(STUDENTS_DATA_NAME)
                .orderBy(FieldPath.of("name", "last"), Query.Direction.ASCENDING)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    firebaseFirestoreException?.let {
                        emitter.onError(it)
                    }
                    emitter.onSuccess(querySnapshot!!.query)
                }
            emitter.setCancellable {
                listener.remove()
            }
        }

    }

    fun deleteStudent(section: String, student: Student): Completable {

        val studentDeleteTask = Completable.create { emitter ->
            sectionRef
                .document(section)
                .collection(STUDENTS_DATA_NAME)
                .document(student.lrn)
                .delete()
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { emitter.onError(it) }
        }

//        val attendanceDeleteTask = Completable.create { emitter ->
//
//            firestoreDB.collectionGroup(student.lrn).get()
//                .addOnSuccessListener { querySnapshot ->
//
//                    firestoreDB.runBatch { writeBatch ->
//                        querySnapshot.forEach { queryDocumentSnapshot ->
//                            writeBatch.delete(queryDocumentSnapshot.reference)
//                        }
//                    }
//                        .addOnSuccessListener { emitter.onComplete() }
//                        .addOnFailureListener { emitter.onError(it) }
//
//                }.addOnFailureListener { emitter.onError(it) }
//        }

        val attendanceDeleteTask = Completable.create { emitter ->
            sectionRef
                .document(section)
                .collection(ATTENDANCE_DATA_NAME)
                .whereEqualTo(FieldPath.of(student.lrn, "lrn"), student.lrn)
                .get()
                .addOnSuccessListener {
                    firestoreDB.runBatch { write ->
                        it.forEach { documentSnapshot ->
                            val deleteMap = hashMapOf<String, Any>()
                            deleteMap[student.lrn] = FieldValue.delete()
                            write.update(documentSnapshot.reference, deleteMap)
                        }
                    }.addOnSuccessListener {
                        emitter.onComplete()
                    }.addOnFailureListener { emitter.onError(it) }
                }.addOnFailureListener { emitter.onError(it) }
        }

        return Completable.merge(listOf(studentDeleteTask, attendanceDeleteTask))

    }


    fun addStudent(section: String, student: Student): Completable {
        return Completable.create { completableEmitter ->
            sectionRef
                .document(section)
                .collection(STUDENTS_DATA_NAME)
                .document(student.lrn)
                .set(student)
                .addOnSuccessListener {
                    completableEmitter.onComplete()
                }.addOnFailureListener {
                    completableEmitter.onError(it)
                }
        }
    }

//    fun getStudentByLrn(section: String, lrn: String): Maybe<Student> {
////        return Single.create {emitter ->
////            sectionRef.document(section).collection(STUDENTS_DATA_NAME)
////                .whereEqualTo("lrn", student.lrn)
////                .get()
////                .addOnSuccessListener {
////                    emitter.onSuccess(it.documents[0].reference)
////                }.addOnFailureListener {
////                    emitter.onError(it)
////                }
////        }
//        return getStudentList(section).map { studentList ->
//            val index = studentList.map { it.lrn }.indexOf(lrn)
//            return@map studentList[index]
//        }
//    }


    fun addAttendance(section: String, attendanceList: List<Attendance>): Completable {

        if (attendanceList.isEmpty()) return Completable.complete()
        else {
            val tempMap: MutableMap<String, Attendance> = mutableMapOf()
            val reference = sectionRef.document(section)
                .collection(ATTENDANCE_DATA_NAME)
                .document(getTodayDate().time.toString())

            return Completable.create { emitter ->
                firestoreDB.runTransaction {
                    attendanceList.forEach { attendance ->
                        if (!it.get(reference).contains(attendance.lrn)) {
                            tempMap[attendance.lrn] = attendance
                        }
                    }
                    it.set(reference, tempMap.toMap(), SetOptions.merge())
                }.addOnSuccessListener { emitter.onComplete() }
                    .addOnFailureListener { emitter.onError(it) }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun getAttendanceQuery(section: String, dateStamp: Date): Observable<List<Attendance>> {

//        val currentTimestamp = Timestamp(dateStamp)
//        val tomorrowTimestamp = Timestamp(getTommorowDate(dateStamp))
//        val attendanceQuery =

        return Observable.create { emitter ->
            val listener = sectionRef.document(section)
                .collection(ATTENDANCE_DATA_NAME)
                .document(dateStamp.time.toString())
                .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    firebaseFirestoreException?.let {
                        emitter.onError(it)
                        return@addSnapshotListener
                    }
                    documentSnapshot?.let {
                        it.data?.let { map ->

                            val attendance = map.map { mapEntry ->
                                val entry = (mapEntry.value as HashMap<String, Any>)
                                Attendance(
                                    entry["time_in"] as Timestamp,
                                    entry["reference"] as DocumentReference,
                                    entry["lrn"] as String
                                )
                            }

                            emitter.onNext(attendance)
                        } ?: emitter.onNext(emptyList())
                    } ?: emitter.onNext(listOf())
                }
            emitter.setCancellable { listener.remove() }
        }

//                .whereLessThan(
//                    "time_in",
//                    tomorrowTimestamp
//                )
//                .whereGreaterThanOrEqualTo("time_in", currentTimestamp)
//                .orderBy("time_in", Query.Direction.ASCENDING)

//        return attendanceQuery


    }
}