package com.test.android.digitalpassserver

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.test.android.digitalpassserver.model.CreateUserResponse
import com.test.android.digitalpassserver.model.Credential
import com.test.android.digitalpassserver.model.CredentialType
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.pipeline.*
import java.security.Key
import java.util.*
import java.util.concurrent.TimeUnit

private const val ONGOING_NOTIFICATION_ID: Int = 1919
private const val CHANNEL_DEFAULT_IMPORTANCE: String = "default"

class DigitalServerService : Service() {
    private lateinit var server: ApplicationEngine
    override fun onCreate() {
        super.onCreate()
        startForeground()
    }

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startServer()
        return START_STICKY
    }

    private fun startServer() {
      server=   embeddedServer(Netty, 8080) {
            install(ContentNegotiation) {
                gson {}
            }
            routing {
                get("/") {
                    call.respond(mapOf("message" to "Hello my future colleague"))
                }
                get("/createAccount") {
                    call.respond(
                        HttpStatusCode.OK,
                        CreateUserResponse.create(this@DigitalServerService)
                    )
                }

                get("/createCredential/{credentialType}") {
                    handleCreateCredentialRoute(this)
                }

            }
        }.start()
    }

    private suspend fun handleCreateCredentialRoute(pipelineContext: PipelineContext<Unit, ApplicationCall>) {

        with(pipelineContext.call) {
            when (parameters["credentialType"]?.lowercase()) {
                "user" -> sendCredential(CredentialType.USER, pipelineContext)
                "ready" -> sendCredential(CredentialType.READY, pipelineContext)
                else -> respond(HttpStatusCode.BadRequest)
            }
        }

    }

    private suspend fun sendCredential(
        type: CredentialType,
        pipelineContext: PipelineContext<Unit, ApplicationCall>
    ) {
        pipelineContext.call.respond(HttpStatusCode.OK, Credential(type, createJWT(type)))
    }


    fun createJWT(type: CredentialType): String {

        val key: Key = Keys.secretKeyFor(SignatureAlgorithm.HS256)

        val currentTime = Date()
        val expMins = if (type == CredentialType.READY) 3 else 5
        val expirationTime = Date(currentTime.time + expMins * 1000 * 60)
        return Jwts.builder()
            .setSubject("Credentail")
            .setId(UUID.randomUUID().toString())
            .claim("type", type.name.lowercase())
            .setExpiration(expirationTime)
            .setIssuedAt(currentTime)
            .signWith(key)
            .compact()

    }


    private fun startForeground() {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }
        val channledID = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                CHANNEL_DEFAULT_IMPORTANCE,
                "server",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                    this
                )
            }

            CHANNEL_DEFAULT_IMPORTANCE
        } else CHANNEL_DEFAULT_IMPORTANCE


        val notification: Notification =
            NotificationCompat.Builder(this, channledID)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setOngoing(false)
                .build()


// Notification ID cannot be 0.
        startForeground(ONGOING_NOTIFICATION_ID, notification)
    }


    override fun onDestroy() {
        super.onDestroy()
        server.stop(120,240,TimeUnit.SECONDS)
        stopForeground(true)
    }

}