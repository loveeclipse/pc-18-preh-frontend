package it.unibo.preh_frontend.utils

import android.os.Handler
import android.util.Log
import it.unibo.preh_frontend.model.dt_model.Anagraphic
import it.unibo.preh_frontend.model.dt_model.Drug
import it.unibo.preh_frontend.model.dt_model.EventInformation
import it.unibo.preh_frontend.model.dt_model.InjectionTreatment
import it.unibo.preh_frontend.model.dt_model.IppvTreatment
import it.unibo.preh_frontend.model.dt_model.MissionInformation
import it.unibo.preh_frontend.model.dt_model.OngoingMissions
import it.unibo.preh_frontend.model.dt_model.PatientData
import it.unibo.preh_frontend.model.dt_model.PatientStatus
import it.unibo.preh_frontend.model.dt_model.ReturnInformation
import it.unibo.preh_frontend.model.dt_model.SimpleTreatment
import it.unibo.preh_frontend.model.dt_model.TrackingStep
import it.unibo.preh_frontend.model.dt_model.VitalParameters
import it.unibo.preh_frontend.utils.api.DiscoveryApi
import it.unibo.preh_frontend.utils.api.EventPreHApi
import it.unibo.preh_frontend.utils.api.MissionPreHApi
import it.unibo.preh_frontend.utils.api.PatientPreHApi
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitClient {

    private const val DELAY_IN_MILLIS = 1000L

    lateinit var patientServiceUrl: String
    lateinit var eventServiceUrl: String
    lateinit var missionServiceUrl: String
    private const val discoveryServiceUrl: String = "https://pc-18-preh-discovery.herokuapp.com/"

    var eventService: EventPreHApi? = null
    var missionService: MissionPreHApi? = null
    var patientService: PatientPreHApi? = null

    private val okHttpClient = OkHttpClient()

    var retrofitClient = Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)

    var discoveryService: DiscoveryApi = retrofitClient.baseUrl(discoveryServiceUrl).build().create(DiscoveryApi::class.java)

    fun obtainServiceLocation() {
        Log.d("ABCDE", "ON obtainServiceLocation ")
        discoveryService.getService("patients-service").enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>?, response: Response<String>?) {
                Log.d("ABCDE", "patients start")
                patientServiceUrl = response!!.body().toString()
                patientService = retrofitClient.baseUrl(patientServiceUrl).build().create(PatientPreHApi::class.java)
                Log.d("ABCDE", "patients end")
            }

            override fun onFailure(call: Call<String>?, t: Throwable?) {
                call?.clone()?.enqueue(this)
                t!!.printStackTrace()
            }
        })
        discoveryService.getService("events-service").enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>?, response: Response<String>?) {
                Log.d("ABCDE", "event start")
                eventServiceUrl = response!!.body().toString()
                eventService = retrofitClient.baseUrl(eventServiceUrl).build().create(EventPreHApi::class.java)
                Log.d("ABCDE", "event end")
            }

            override fun onFailure(call: Call<String>?, t: Throwable?) {
                call?.clone()?.enqueue(this)
                t!!.printStackTrace()
            }
        })

        discoveryService.getService("missions-service").enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>?, response: Response<String>?) {
                Log.d("ABCDE", "mission start")
                missionServiceUrl = response!!.body().toString()
                missionService = retrofitClient.baseUrl(missionServiceUrl).build().create(MissionPreHApi::class.java)
                Log.d("ABCDE", "mission end")
            }

            override fun onFailure(call: Call<String>?, t: Throwable?) {
                call?.clone()?.enqueue(this)
                t!!.printStackTrace()
            }
        })
    }

    fun createPatient(patientData: PatientData) {
        if (patientService != null) {
            patientService!!.postNewPatient(patientData).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>?, response: Response<String>?) {
                    DtIdentifiers.patientId = response!!.body().toString()
                }

                override fun onFailure(call: Call<String>?, t: Throwable?) {
                    call!!.clone().enqueue(this)
                    t!!.printStackTrace()
                }
            })
        } else {
            Log.e("TEST", "Service not init.")
            Handler()
                    .postDelayed({ createPatient(patientData) }, DELAY_IN_MILLIS)
        }
    }

    fun putAnagraphicData(anagraphic: Anagraphic) {
        if (DtIdentifiers.patientId != null && patientService != null) {
            patientService!!.putPatientAnagraphic(DtIdentifiers.patientId!!, anagraphic).enqueue(BasicVoidCallback)
        } else {
            Log.e("BAD_ID", "The given patientId was not yet set")
            Handler()
                    .postDelayed({ putAnagraphicData(anagraphic) }, DELAY_IN_MILLIS)
        }
    }

    fun sendTrackingStep(trackingStep: String, trackingStepItem: TrackingStep) {
        if (DtIdentifiers.assignedMission != null && missionService != null) {
            Log.d("TEST", "MISSIONID    ${DtIdentifiers.assignedMission}")
            Log.d("TEST", "TRACKINGSTEP    $trackingStep")
            missionService!!.putNewTrackingStep(DtIdentifiers.assignedMission!!, trackingStep, trackingStepItem)
                    .enqueue(BasicVoidCallback)
        } else {
            Log.e("BAD_ID", "The given missionId was not yet set")
            Handler()
                    .postDelayed({ sendTrackingStep(trackingStep, trackingStepItem) }, DELAY_IN_MILLIS)
        }
    }

    fun sendReturnInformation(returnInformation: ReturnInformation) {
        if (DtIdentifiers.assignedMission != null && missionService != null) {
            missionService!!.insertReturnInformation(DtIdentifiers.assignedMission!!,
                    returnInformation).enqueue(BasicVoidCallback)
        } else {
            Log.e("BAD_ID", "The given missionId was not yet set")
            Handler()
                    .postDelayed({ sendReturnInformation(returnInformation) }, DELAY_IN_MILLIS)
        }
    }

    fun putMissionMedic() {
        if (DtIdentifiers.assignedMission != null && missionService != null) {
            missionService!!.putMissionMedic(DtIdentifiers.assignedMission!!,
                    DtIdentifiers.doctor!!).enqueue(BasicVoidCallback)
        } else {
            Log.e("BAD_ID", "The given missionId was not yet set")
            Handler()
                    .postDelayed({
                        Log.e("RETRAY", "RETRY medic")
                        putMissionMedic() }, DELAY_IN_MILLIS)
        }
    }

    fun putMissionOngoingState(ongoingState: Boolean) {
        if (DtIdentifiers.assignedMission != null && missionService != null) {
            missionService!!.putOngoingStatus(DtIdentifiers.assignedMission!!, ongoingState).enqueue(BasicVoidCallback)
        } else {
            Log.e("BAD_ID", "The given missionId was not yet set")
            Handler().postDelayed({ putMissionOngoingState(ongoingState) }, DELAY_IN_MILLIS)
        }
    }

    fun getOngoingMissionsByVehicle() {
        if (DtIdentifiers.vehicle != null && missionService != null) {
            missionService!!
                    .getOngoingMissionsByVehicle(DtIdentifiers.vehicle!!)
                    .enqueue(object : Callback<OngoingMissions> {
                override fun onFailure(call: Call<OngoingMissions>, t: Throwable) {}

                override fun onResponse(call: Call<OngoingMissions>, response: Response<OngoingMissions>) {
                    if (response.code() == 200) {
                        val ongoingMissions = OngoingMissions(response.body()!!.ids, response.body()!!.links)
                        Log.d("TEST", "MISIONID   ${ongoingMissions.ids[0]}")
                        DtIdentifiers.assignedMission = ongoingMissions.ids[0]
                        getMissionInformation()
                        putMissionMedic()
                    }
                }
            })
        } else {
            Log.e("BAD_ID", "The given vehicleId was not yet set")
            Handler().postDelayed({
                Log.e("RETRY", "RETRY Mission")
                getOngoingMissionsByVehicle() }, DELAY_IN_MILLIS)
        }
    }

    fun getOngoingMissionsByEventId() {
        if (DtIdentifiers.assignedEvent != null && missionService != null) {
            missionService!!
                    .getOngoingMissionsByEventId(DtIdentifiers.assignedEvent!!)
                    .enqueue(object : Callback<OngoingMissions> {
                override fun onFailure(call: Call<OngoingMissions>, t: Throwable) {
                }

                override fun onResponse(call: Call<OngoingMissions>, response: Response<OngoingMissions>) {
                    if (response.code() == 200) {
                        val ongoingMissions = OngoingMissions(response.body()!!.ids, response.body()!!.links)
                        CurrentEventInfo.involvedVehicles = ongoingMissions.ids.size
                        Log.d("TEST", "NUMERO MEZZI    ${CurrentEventInfo.involvedVehicles}")
                    }
                }
            })
        } else {
            Log.e("BAD_ID", "The given eventId was not yet set")
            Handler().postDelayed({
                Log.e("RETRY", "RETRY count mission")
                getOngoingMissionsByEventId() }, DELAY_IN_MILLIS)
        }
    }

    fun getMissionInformation() {
        if (DtIdentifiers.assignedMission != null && missionService != null) {
            missionService!!
                    .getMissionInformation(DtIdentifiers.assignedMission!!)
                    .enqueue(object : Callback<MissionInformation> {
                override fun onFailure(call: Call<MissionInformation>, t: Throwable) {
                    t.printStackTrace()
                    Log.d("TEST", call.request().url().toString())
                }

                override fun onResponse(call: Call<MissionInformation>, response: Response<MissionInformation>?) {
                    DtIdentifiers.assignedEvent =
                            response!!
                            .body()!!.eventId
                    getEventInformation()
                    Log.d("TEST", "EVENTID     ${DtIdentifiers.assignedEvent}")
                }
            })
        } else {
            Log.e("BAD_ID", "The given missionId was not yet set")
            Handler()
                    .postDelayed({
                        Log.e("RETRY", "RETRY mission")
                        getMissionInformation() }, DELAY_IN_MILLIS)
        }
    }

    fun getEventInformation() {
        if (DtIdentifiers.assignedEvent != null && eventService != null) {
            eventService!!
                    .getEventData(DtIdentifiers.assignedEvent!!)
                    .enqueue(object : Callback<EventInformation> {
                override fun onResponse(call: Call<EventInformation>, response: Response<EventInformation>) {
                    val eventInformation: EventInformation = response.body()!!
                    CurrentEventInfo.set(eventInformation)
                    getOngoingMissionsByEventId()
                }

                override fun onFailure(call: Call<EventInformation>, t: Throwable) {
                    t.printStackTrace()
                    Log.d("TEST", call.request().url().toString())
                }
            })
        } else {
            Log.e("BAD_ID", "The given eventId was not yet set")
            Handler()
                    .postDelayed({
                        Log.e("RETRY", "RETRY event")
                        getEventInformation() }, DELAY_IN_MILLIS)
        }
    }

    fun putPatientStatus(patientStatus: PatientStatus) {
        if (DtIdentifiers.patientId != null && patientService != null) {
            patientService!!
                    .putPatientStatus(DtIdentifiers.patientId!!, patientStatus)
                    .enqueue(BasicVoidCallback)
        } else {
            Log.e("BAD_ID", "The given patientId was not yet set")
            Handler()
                    .postDelayed({ putPatientStatus(patientStatus) }, DELAY_IN_MILLIS)
        }
    }

    fun postPatientVitalParameters(vitalParameters: VitalParameters) {
        if (DtIdentifiers.patientId != null && patientService != null) {
            patientService!!
                    .postVitalParameters(DtIdentifiers.patientId!!, vitalParameters)
                    .enqueue(BasicStringCallback)
        } else {
            Log.e("BAD_ID", "The given patientId was not yet set")
            Handler()
                    .postDelayed({ postPatientVitalParameters(vitalParameters) }, DELAY_IN_MILLIS)
        }
    }

    fun postDrugs(drug: Drug) {
        if (DtIdentifiers.patientId != null && patientService != null) {
        patientService!!
                .postDrugs(DtIdentifiers.patientId!!, drug)
                .enqueue(BasicStringCallback)
        } else {
            Log.e("BAD_ID", "The given patientId was not yet set")
            Handler()
                    .postDelayed({ postDrugs(drug) }, DELAY_IN_MILLIS)
        }
    }

    fun postSimpleManeuver(simpleManeuver: String, executionTime: String) {
        if (DtIdentifiers.patientId != null && patientService != null) {
            patientService!!
                    .postSimpleManeuver(DtIdentifiers.patientId!!, simpleManeuver, executionTime)
                    .enqueue(BasicVoidCallback)
        } else {
            Log.e("BAD_ID", "The given patientId was not yet set")
            Handler().postDelayed({ postSimpleManeuver(simpleManeuver, executionTime) }, DELAY_IN_MILLIS)
        }
    }

    fun deleteSimpleManeuver(simpleManeuver: String) {
        if (DtIdentifiers.patientId != null && patientService != null) {
            patientService!!
                    .deleteSimpleManeuver(DtIdentifiers.patientId!!, simpleManeuver)
                    .enqueue(BasicVoidCallback)
        } else {
            Log.e("BAD_ID", "The given patientId was not yet set")
            Handler().postDelayed({ deleteSimpleManeuver(simpleManeuver) }, DELAY_IN_MILLIS)
        }
    }

    fun postSimpleTreatment(simpleTreatment: SimpleTreatment) {
        if (DtIdentifiers.patientId != null && patientService != null) {
            patientService!!
                    .postSimpleTreatment(DtIdentifiers.patientId!!, simpleTreatment)
                    .enqueue(BasicStringCallback)
        } else {
            Log.e("BAD_ID", "The given patientId was not yet set")
            Handler().postDelayed({ postSimpleTreatment(simpleTreatment) }, DELAY_IN_MILLIS)
        }
    }

    fun postInjectionTreatment(injectionTreatment: InjectionTreatment) {
        if (DtIdentifiers.patientId != null && patientService != null) {
            patientService!!
                    .postInjectionTreatment(DtIdentifiers.patientId!!, injectionTreatment)
                    .enqueue(BasicStringCallback)
        } else {
            Log.e("BAD_ID", "The given patientId was not yet set")
            Handler().postDelayed({ postInjectionTreatment(injectionTreatment) }, DELAY_IN_MILLIS)
        }
    }

    fun postIppvTreatment(ippvTreatment: IppvTreatment) {
        if (DtIdentifiers.patientId != null && patientService != null) {
            patientService!!
                    .postIppvTreatment(DtIdentifiers.patientId!!, ippvTreatment)
                    .enqueue(BasicStringCallback)
        } else {
            Log.e("BAD_ID", "The given patientId was not yet set")
            Handler().postDelayed({ postIppvTreatment(ippvTreatment) }, DELAY_IN_MILLIS)
        }
    }

    fun postComplication(complication: String, time: String) {
        if (DtIdentifiers.patientId != null && patientService != null) {
            patientService!!
                    .postComplication(DtIdentifiers.patientId!!, complication, time)
                    .enqueue(BasicVoidCallback)
        } else {
            Log.e("BAD_ID", "The given patientId was not yet set")
            Handler().postDelayed({ postComplication(complication, time) }, DELAY_IN_MILLIS)
        }
    }

    fun deleteComplication(complication: String) {
        if (DtIdentifiers.patientId != null && patientService != null) {
            patientService!!
                    .deleteComplication(DtIdentifiers.patientId!!, complication)
                    .enqueue(BasicVoidCallback)
        } else {
            Log.e("BAD_ID", "The given patientId was not yet set")
            Handler().postDelayed({ deleteComplication(complication) }, DELAY_IN_MILLIS)
        }
    }

    object BasicVoidCallback : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            Log.d("TEST", call.request().url().toString())
            Log.d("TEST", response.code().toString())
        }

        override fun onFailure(call: Call<Void>, t: Throwable) {
            call.clone().enqueue(this)
            t.printStackTrace()
            Log.d("TEST", call.request().url().toString())
        }
    }

    object BasicStringCallback : Callback<String> {
        override fun onResponse(call: Call<String>, response: Response<String>) {
            Log.d("TEST", call.request().url().toString())
            Log.d("TEST", response.code().toString())
        }

        override fun onFailure(call: Call<String>, t: Throwable) {
            call.clone().enqueue(this)
            t.printStackTrace()
            Log.d("TEST", call.request().url().toString())
        }
    }
}