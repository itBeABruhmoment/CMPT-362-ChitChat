package com.example.cmpt_362_chitchat.data

import android.util.Log
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import java.lang.Exception

// do multiple queries with one callback
class CombinedQuery {
    private lateinit var queriesToDo: ArrayList<SingularQuery>
    private lateinit var onComplete: (failed: ArrayList<SingularQuery>) -> Unit
    private var numDone: Int = 0
    private var failedQueries: ArrayList<SingularQuery> = ArrayList()

    constructor(
        queriesToDo: ArrayList<SingularQuery>,
        onComplete: (failed: ArrayList<SingularQuery>) -> Unit
        ) {
        this.queriesToDo = queriesToDo
        this.onComplete = onComplete
        Log.d("combined", "${queriesToDo.size} queries")
        for(query: SingularQuery in queriesToDo) {
            Log.d("combined", "query")
            var task:Task<DataSnapshot> = query.node.get()

            val onSuccess = query.onSuccess
            val onFail = query.onFail
            if(onSuccess != null) {
                Log.d("combined", "add onSuccess")
                task = task.addOnSuccessListener(CombinedQueryOnSuccess(onSuccess))
            }
            if(onFail != null) {
                Log.d("combined", "add onFail")
                task.addOnFailureListener(CombinedQueryOnFail(onFail, query))
            }
        }

    }

    @Synchronized
    private fun queryDone() {
        numDone++
        Log.d("combined", "done $numDone")
        if(numDone == queriesToDo.size) {
            onComplete(failedQueries)
        }
    }

    @Synchronized
    private fun failedQueryOperation(run: (ArrayList<SingularQuery>) -> Unit) {
        run(failedQueries)
    }

    private inner class CombinedQueryOnSuccess(
        var onSuccessLis: OnSuccessListener<DataSnapshot?>,
        ) : OnSuccessListener<DataSnapshot?> {

        override fun onSuccess(snaphot: DataSnapshot?) {
            onSuccessLis.onSuccess(snaphot)
            queryDone()
        }
    }

    private inner class CombinedQueryOnFail(
        var onFail: OnFailureListener,
        var query: SingularQuery
    ) : OnFailureListener {
        override fun onFailure(exception: Exception) {
            onFail.onFailure(exception)
            failedQueryOperation {
                it.add(query)
            }
            queryDone()
        }
    }
}

class SingularQuery(
    var node: DatabaseReference,
    var onSuccess: OnSuccessListener<DataSnapshot?>?,
    var onFail: OnFailureListener?
) {
}