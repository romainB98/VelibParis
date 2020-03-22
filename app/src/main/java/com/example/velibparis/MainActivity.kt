package com.example.velibparis
import android.content.Context
import android.os.Bundle
import android.util.JsonReader
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.InputStreamReader
import java.net.URL
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
/* Code originale de Nils Vaede que j'ai retravaillé en long en large et en travers afin de comprendre et de faire mon propre code  */

class MainActivity : AppCompatActivity() {
    val helper = MySQLiteHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listView = findViewById<ListView>(R.id.listOfBike)
        getLieux()
        fun getdateRefresh (){
            //https://developer.android.com/reference/java/text/SimpleDateFormat.html

            val dateFormat: DateFormat = SimpleDateFormat("HH:mm:ss")
            val dateInfo = Date()
            val dateformatted: String = dateFormat.format(dateInfo)
            val dateVue = findViewById<TextView>(R.id.lastDate)

            dateVue.text = "Dernière MàJ:" + dateformatted
        }

        fun popUpRefesh (){
            //https://developer.android.com/guide/topics/ui/notifiers/toasts?hl=fr#kotlin
            val text = "Page rafraîchit !"
            val duration = Toast.LENGTH_SHORT
            val popUp = Toast.makeText(applicationContext, text, duration)
            popUp.show()

        }


        findViewById<ImageButton>(R.id.refreshBtn).setOnClickListener {
            getLieux()
            getdateRefresh ()
            popUpRefesh ()

        }

    }
    class StationDeVelib(stationId: String?, lieu: String, nbVelib: Int, gps: HashMap<String, Double>){
        val stationId: String? = stationId
        val lieu: String = lieu
        var nbVelib: Int = nbVelib
        val gps: HashMap<String, Double> = gps
    }
    fun getReadData(){
        val velibUrl = URL("https://opendata.paris.fr/api/records/1.0/search/?dataset=velib-disponibilite-en-temps-reel&facet=name&facet=is_installed&facet=is_renting&facet=is_returning&facet=nom_arrondissement_communes")
        val connect = velibUrl .openConnection()
        val infoReader = JsonReader(InputStreamReader(connect.getInputStream()))
        infoReader.beginObject()



        while(infoReader.hasNext()) {
            val lieu = infoReader.nextName()
            if(lieu.equals("records")) {
                infoReader.beginArray()
                while(infoReader.hasNext()) {
                    infoReader.beginObject()
                    var stationId: String? = null
                    var lieu = "Lieu inconnu"
                    var nbVelib = 0
                    var gps = HashMap<String, Double>()
                    while(infoReader.hasNext()) {
                        var lieuEnregistre = infoReader.nextName()
                        if (lieuEnregistre.equals("fields")) {
                            infoReader.beginObject()
                            while (infoReader.hasNext()) {

                                var fields_name = infoReader.nextName()
                                if (fields_name.equals("stationcode")) {
                                    stationId = infoReader.nextString()
                                }
                                else if(fields_name.equals("name")) {
                                    lieu = infoReader.nextString()
                                } else if(fields_name.equals("numbikesavailable")) {
                                    nbVelib = infoReader.nextInt()
                                } else if(fields_name.equals("coordonnees_geo")) {
                                    infoReader.beginArray()
                                    gps["lat"] = infoReader.nextDouble()
                                    gps["lon"] = infoReader.nextDouble()
                                    infoReader.endArray()
                                } else {
                                    infoReader.skipValue()
                                }
                            }
                            infoReader.endObject()
                        } else {
                            infoReader.skipValue()
                        }
                    }
                    val station = StationDeVelib(stationId, lieu, nbVelib, gps)
                    helper.insertStation(station)
                    infoReader.endObject()
                }
                infoReader.endArray()
            } else {
                infoReader.skipValue()
            }
        }
        infoReader.endObject()
    }
    fun getDataToBase(){
        runOnUiThread {
            val adapter = CustomAdapter(this, this, 0)


            val result = helper.getAllStations()
            if (result != null) {
                result.moveToFirst()
                while (result.moveToNext()) {
                    val stationId = result.getString(result.getColumnIndex("id"))
                    val lieu = result.getString(result.getColumnIndex("name"))
                    val nbVelib = result.getString(result.getColumnIndex("nbr_bike")).toInt()
                    val gps = result.getString(result.getColumnIndex("coordinates"))

                    // convert string to HashMap
                    val long = gps.split("{lon=")[1].split(",")[0]
                    val lat = gps.split("lat=")[1].split("}")[0]

                    val gpsHashMap = HashMap<String, Double>()
                    gpsHashMap["lat"] = lat.toDouble()
                    gpsHashMap["lon"] = long.toDouble()

                    val station = StationDeVelib(stationId, lieu, nbVelib, gpsHashMap)
                    adapter.add(station)
                }
                result.close()
            }
            listOfBike.adapter = adapter}


    }

    fun getLieux() {
        val t = Thread(Runnable {
            try {
                getReadData()
                getDataToBase()

            } catch (e: Exception) {
                Log.e("error", e.message ?: "No message but error")
                e.printStackTrace()
            }
        })
        t.start()
    }

    class CustomAdapter(activity: MainActivity,ctx: Context, resid: Int) : ArrayAdapter<StationDeVelib>(ctx, resid) {
        var thisActivity = activity

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val vue = convertView ?: thisActivity.layoutInflater.inflate(R.layout.line, null)

            var lieu = this.getItem(position)
            if(lieu != null) {
                vue.findViewById<TextView>(R.id.lieu).text = lieu.lieu
                vue.findViewById<TextView>(R.id.nbVelib).text = lieu.nbVelib.toString()
            }

            return vue
        }
    }









}
