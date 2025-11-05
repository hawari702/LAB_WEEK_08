package com.example.lab_week_08.worker

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

class SecondWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // Ambil input data
        val id = inputData.getString(INPUT_DATA_ID)

        // Simulasikan proses yang memakan waktu 3 detik
        Thread.sleep(3000L)

        // Siapkan output data
        val outputData = Data.Builder()
            .putString(OUTPUT_DATA_ID, id)
            .build()

        // Kembalikan hasil sukses
        return Result.success(outputData)
    }

    companion object {
        const val INPUT_DATA_ID = "inId"
        const val OUTPUT_DATA_ID = "outId"
    }
}
