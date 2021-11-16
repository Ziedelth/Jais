/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.debug

import com.sun.management.OperatingSystemMXBean
import fr.ziedelth.jais.utils.FileImpl
import java.lang.management.ManagementFactory

class JRecord(val name: String) {
    private var record: Boolean = false
    private val operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
    private val runtime = Runtime.getRuntime()
    private var start: Long? = null
    private var i = 0
    private var sumProcessCpuLoad: Double? = null
    private var sumProcessRamTotal: Long? = null
    private var sumProcessRamFree: Long? = null

    fun start() {
        if (!this.record) {
            this.record = true

            JThread.start({
                this.start = System.currentTimeMillis()

                while (this.record) {
                    if (this.sumProcessCpuLoad == null) this.sumProcessCpuLoad =
                        this.operatingSystemMXBean.processCpuLoad
                    else this.sumProcessCpuLoad?.plus(this.operatingSystemMXBean.processCpuLoad)

                    if (this.sumProcessRamTotal == null) this.sumProcessRamTotal = this.runtime.totalMemory()
                    else this.sumProcessRamTotal?.plus(this.runtime.totalMemory())

                    if (this.sumProcessRamFree == null) this.sumProcessRamFree = this.runtime.freeMemory()
                    else this.sumProcessRamFree?.plus(this.runtime.freeMemory())
                    this.i++
                }

                JLogger.info("----------[ RECORD ${this.name.uppercase()} DUMP ]----------")
                JLogger.config(
                    "CPU Load: ${
                        String.format(
                            "%.4f",
                            this.sumProcessCpuLoad?.div(this.i.toDouble())?.times(100.0)
                        )
                    }%"
                )
                JLogger.config(
                    "RAM Used: ${
                        FileImpl.toFormat(
                            (this.sumProcessRamTotal!!.minus(this.sumProcessRamFree!!)).div(
                                this.i
                            )
                        )
                    }"
                )
                JLogger.config("End in: ${System.currentTimeMillis() - this.start!!}ms")

                this.start = null
                this.i = 0
                this.sumProcessCpuLoad = null
                this.sumProcessRamTotal = null
                this.sumProcessRamFree = null
            })
        }
    }

    fun stop() {
        if (this.record) this.record = false
    }
}