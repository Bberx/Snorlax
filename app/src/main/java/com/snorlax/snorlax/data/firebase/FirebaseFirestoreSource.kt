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

import com.google.firebase.firestore.*
import com.google.gson.Gson
import com.snorlax.snorlax.model.*
import com.snorlax.snorlax.utils.Constants
import com.snorlax.snorlax.utils.TimeUtils.getMaxMonthDate
import com.snorlax.snorlax.utils.TimeUtils.getTodayDateUTC
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

object FirebaseFirestoreSource {

    private const val STUDENTS_DATA_NAME = "student"
    private const val SECTIONS_DATA_NAME = "section"
    private const val ATTENDANCE_DATA_NAME = "attendance"
    private const val USER_DATA_NAME = "user"

    private val firestoreDB = FirebaseFirestore.getInstance()

    private val userRef = firestoreDB.collection(USER_DATA_NAME)
    private val sectionRef = firestoreDB.collection(SECTIONS_DATA_NAME)

    private val gson = Gson()

    fun getSection(section: String): Single<Section> {
        return Single.create { emitter ->
            sectionRef.document(section)
                .get()
                .addOnSuccessListener {
                    emitter.onSuccess(it.toObject(Section::class.java)!!)
                }.addOnFailureListener {
                    emitter.onError(it)
                }
        }
    }

    fun getAdmin(uid: String): Single<User> {
//        if (uid == null) return Single.just(null)
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
        }
//            .andThen(writeSection(user.section))
            .subscribeOn(Schedulers.io())
    }

    fun updateLateData(section: String, lateData: LateData): Completable {
        return Completable.create { emitter ->
            sectionRef.document(section)
                .set(lateData, SetOptions.merge())
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { emitter.onError(it) }
        }
    }

    fun getAttendanceObservable(
        section: String,
        dateStamp: Date
    ): Observable<List<Attendance>> {

        return Observable.create<List<Attendance>> { emitter ->
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
                            val raw = map.map { mapEntry ->
                                val entry = mapEntry.value as HashMap<*, *>
                                gson.fromJson(gson.toJsonTree(entry), Attendance::class.java)
                            }
                            val attendance = raw.sortedByDescending { attendance ->
                                attendance.time_in
                            }
                            emitter.onNext(attendance)
                        } ?: emitter.onNext(emptyList())
                    } ?: emitter.onNext(emptyList())
                }
            emitter.setCancellable {
                listener.remove()
            }
        }.subscribeOn(Schedulers.io()).unsubscribeOn(AndroidSchedulers.mainThread())
    }

    fun getLateData(section: String): Observable<LateData> {
        return Observable.create<LateData> { emitter ->
            val listener = sectionRef.document(section)
                .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    firebaseFirestoreException?.let {
                        emitter.onError(it)
                        return@addSnapshotListener
                    }
                    documentSnapshot!!.data?.let { data ->
                        if (data.isNotEmpty()) {
                            val lateData = documentSnapshot.toObject(LateData::class.java)!!
                            emitter.onNext(lateData)
                        } else emitter.onNext(Constants.SECTION_LIST.getValue(section).late_data!!)
                    }
                }
            emitter.setCancellable {
                listener.remove()
            }
        }.subscribeOn(Schedulers.io()).unsubscribeOn(AndroidSchedulers.mainThread())
    }

//    private fun writeSection(section: String): Completable {
//        return Completable.create { emitter ->
//            sectionRef.whereEqualTo(FieldPath.documentId(), section)
//                .whereEqualTo("display_name", Constants.SECTION_LIST.getValue(section).display_name)
//                .get().addOnSuccessListener { query ->
//                    if (query.isEmpty) {
//                        sectionRef
//                            .document(section)
//                            .set(Constants.SECTION_LIST.getValue(section), SetOptions.merge())
//                            .addOnCompleteListener {
//                                if (it.isSuccessful) emitter.onComplete()
//                                else emitter.onError(it.exception!!)
//                            }
//                    } else {
//                        emitter.onComplete()
//                    }
//                }.addOnFailureListener { error ->
//                    emitter.onError(error)
//                }
//        }
//    }

    fun getStudentList(section: String): Single<List<Student>> {
        return Single.create { emitter: SingleEmitter<List<Student>> ->
            getStudentQuery(section).get()
                .addOnSuccessListener {
                    emitter.onSuccess(it.toObjects(Student::class.java))
//                    emitter.onSuccess(it.documents.map { documentSnapshot ->
//                        documentSnapshot.toObject(Student::class.java)!!
//                    })
                }.addOnFailureListener { emitter.onError(it) }
        }
    }

    fun getStudentQuery(section: String): Query {
//        return Single.create<Query> { emitter ->
//            val listener = sectionRef
//                .document(section)
//                .collection(STUDENTS_DATA_NAME)
//                .orderBy(FieldPath.of(Student.NAME_VAL, Student.LAST_NAME_VAL), Query.Direction.ASCENDING)
//                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
//                    firebaseFirestoreException?.let {
//                        emitter.onError(it)
//                    }
//                    emitter.onSuccess(querySnapshot!!.query)
//                }
//            emitter.setCancellable {
//                listener.remove()
//            }
//        }
        return sectionRef.document(section)
            .collection(STUDENTS_DATA_NAME)
            .orderBy(
                FieldPath.of(Student.NAME_VAL, Student.LAST_NAME_VAL),
                Query.Direction.ASCENDING
            )

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


    fun addStudent(
        section: String,
        student: Student
    ): Completable {
        return Completable.create { completableEmitter ->
            sectionRef
                .document(section)
                .collection(STUDENTS_DATA_NAME)
                .document(student.lrn)
                .set(student, SetOptions.merge())
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


    fun addAttendance(
        section: String,
        attendanceList: List<Attendance>
    ): Completable {

        if (attendanceList.isEmpty()) return Completable.complete()
        else {
            val tempMap: MutableMap<String, Attendance> = mutableMapOf()
            val reference = sectionRef.document(section)
                .collection(ATTENDANCE_DATA_NAME)
                .document(getTodayDateUTC().time.toString())

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



    private fun getAttendanceByDocument(document: DocumentSnapshot): List<Attendance> {
        document.data?.let { map ->
            return map.map {
                val entry = it.value as HashMap<*, *>
                gson.fromJson(gson.toJsonTree(entry), Attendance::class.java)
            }
        }
        return emptyList()
    }

    fun getMonthlyAttendance(
        section: String,
        month: Date
    ): Single<List<Attendance>> {
        val attendanceList = mutableListOf<Attendance>()
        val reference = sectionRef.document(section)
            .collection(ATTENDANCE_DATA_NAME)

        return Single.create { emitter ->
            reference.whereGreaterThanOrEqualTo(FieldPath.documentId(), month.time.toString())
                .whereLessThanOrEqualTo(
                    FieldPath.documentId(),
                    getMaxMonthDate(month).time.toString()
                ).get()
                .addOnSuccessListener {

                    it.documents.forEach { document ->
                        attendanceList.addAll(getAttendanceByDocument(document))
                    }
                    emitter.onSuccess(attendanceList.toList())
                }.addOnFailureListener {
                    emitter.onError(it)
                }
        }
    }

//    companion object {
//        private const val STUDENTS_DATA_NAME = "student"
//        private const val SECTIONS_DATA_NAME = "section"
//        private const val ATTENDANCE_DATA_NAME = "attendance"
//        private const val USER_DATA_NAME = "user"
//
//        private var instance: FirebaseFirestoreSource? = null
//
//        fun getInstance(): FirebaseFirestoreSource {
//            instance?.let { return it }
//            instance = FirebaseFirestoreSource()
//            return getInstance()
//        }
//    }
}