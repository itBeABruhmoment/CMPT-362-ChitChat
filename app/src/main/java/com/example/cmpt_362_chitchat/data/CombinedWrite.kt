package com.example.cmpt_362_chitchat.data

import android.util.Log
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DatabaseReference
import java.lang.Exception

// do multiple write operations with one callback
class CombinedWrite {
    private lateinit var writeOperations: ArrayList<SingularWrite>
    private lateinit var onComplete: (failed: ArrayList<SingularWrite>) -> Unit
    private var failedWrites: ArrayList<SingularWrite> = ArrayList()
    private var done: Int = 0;

    constructor(
        writeOperations: ArrayList<SingularWrite>,
        onComplete: (failed: ArrayList<SingularWrite>) -> Unit
    ) {
        this.writeOperations = writeOperations
        this.onComplete = onComplete

        // start queries
        for(write: SingularWrite in writeOperations) {
            write.node.setValue(write.set)
                .addOnSuccessListener(CombinedWriteOnSuccess(write.onSuccessListener))
                .addOnFailureListener(CombinedWriteOnFail(write.onFailureListener, write))
        }

        if(writeOperations.size == 0) {
            onComplete(ArrayList())
        }
    }

    // access failed in thread safe way
    @Synchronized
    private fun failedWriteOperation(run: (ArrayList<SingularWrite>) -> Unit) {
        run(failedWrites)
    }

    // track how many writes finished, run callback when all writes are done
    @Synchronized
    private fun queryDone() {
        done++
        Log.d("combined", "done $done")
        if(done == writeOperations.size) {
            onComplete(failedWrites)
        }
    }

    private inner class CombinedWriteOnSuccess(
        var onSuccessLis: OnSuccessListener<Void>
        ) : OnSuccessListener<Void> {
        override fun onSuccess(param: Void?) {
            onSuccessLis.onSuccess(param)
            queryDone()
        }
    }

    private inner class CombinedWriteOnFail(
        var onFailLis: OnFailureListener,
        var singularWrite: SingularWrite
    ) : OnFailureListener {
        override fun onFailure(exception: Exception) {
            onFailLis.onFailure(exception)
            failedWriteOperation {
                it.add(singularWrite)
            }
            queryDone()
        }
    }
}

class SingularWrite(
    var set: Any,
    var node: DatabaseReference,
    var onSuccessListener: OnSuccessListener<Void>,
    var onFailureListener: OnFailureListener,
    ) {

}