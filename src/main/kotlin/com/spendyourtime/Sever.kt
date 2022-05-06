package com.spendyourtime

import com.spendyourtime.data.*
import com.spendyourtime.data.Map
import com.spendyourtime.helpers.*
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.plugin.openapi.*
import io.javalin.plugin.openapi.annotations.*
import io.javalin.plugin.openapi.ui.ReDocOptions
import io.javalin.plugin.openapi.ui.SwaggerOptions
import io.swagger.v3.oas.models.info.Info
import org.slf4j.LoggerFactory
import java.util.*

private fun getOpenApiOptions(): OpenApiOptions {
    val applicationInfo: Info = Info().version("1.0").description("SpendYourTime API")
    return OpenApiOptions(applicationInfo).path("/api-docs")
        .swagger(SwaggerOptions("/swagger").title("Swagger SpendYourTime API"))
        .reDoc(ReDocOptions("/redoc").title("ReDoc SpendYourTime API"))
}


object Server {
    @JvmStatic
    fun main(args: Array<String>) {
        val logger = LoggerFactory.getLogger(this::class.java)
        Jsonbase.loadFromJSON()

        val app = Javalin.create {
            it.registerPlugin(OpenApiPlugin(getOpenApiOptions()))
        }.apply {
            exception(Exception::class.java) { e, ctx ->
                //ctx.json(e.message.toString())
                logger.error("Exception: ", e)
                ctx.status(500).json("SERVER_ERROR")
            }
        }.start(7000)
        app.routes {

            app.get("info") { ctx ->
                DatabaseConnect.AllUsers.getAllUsers()?.let { ctx.retour(200, it) }
            }

            //LOGIN
            app.post("/login")
            @OpenApi(
                description = "Login a user", tags = ["Users"], formParams = [OpenApiFormParam(
                    name = "pseudo", type = String::class
                ), OpenApiFormParam(name = "password", type = String::class)], responses = [OpenApiResponse(
                    "200", content = [OpenApiContent(type = "Token", from = Token::class)]
                ), OpenApiResponse(
                    "400", content = [OpenApiContent(
                        type = "PSEUDO_IS_EMPTY", from = String::class
                    ), OpenApiContent(
                        type = "PASSWORD_IS_EMPTY", from = String::class
                    ), OpenApiContent(
                        type = "PSEUDO_NOT_EXIST", from = String::class
                    ), OpenApiContent(type = "PASSWORD_NOT_VALID", from = String::class)]
                ), OpenApiResponse(
                    "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                )]
            ) { ctx ->
                logger.info("POST_LOGIN")

                //Check pseudo login
                if (ctx.formParam("pseudo").isNullOrEmpty()) {
                    ctx.retour(400, "PSEUDO_IS_EMPTY")
                    //return@post
                } else if (DatabaseConnect.AllUsers.findUserByPseudo(ctx.formParam("pseudo").toString()) == null) {
                    ctx.retour(400, "PSEUDO_NOT_EXIST")
                    //return@post
                } else if (ctx.formParam("password").isNullOrEmpty()) {
                    ctx.retour(400, "PASSWORD_IS_EMPTY")
                    //return@post
                } else if (!DatabaseConnect.AllUsers.checkPassword(
                        ctx.formParam("pseudo").toString(), ctx.formParam("password").toString()
                    )
                ) {
                    ctx.retour(400, "PASSWORD_NOT_VALID")
                    //return@post
                } else {
                    val u = DatabaseConnect.AllUsers.findUserByPseudo(ctx.formParam("pseudo").toString())!!
                    ctx.retour(200, Certification.create(u))
                }
            }

            //REGISTER
            app.post("/register")
            @OpenApi(
                description = "Register a user", tags = ["Users"], formParams = [OpenApiFormParam(
                    name = "pseudo", type = String::class
                ), OpenApiFormParam(name = "password", type = String::class), OpenApiFormParam(
                    name = "email",
                    type = String::class
                )], responses = [OpenApiResponse(
                    "200", content = [OpenApiContent(type = "Tokeb", from = Token::class)]
                ), OpenApiResponse(
                    "400", content = [OpenApiContent(
                        type = "EMAIL_IS_EMPTY", from = String::class
                    ), OpenApiContent(
                        type = "EMAIL_INVALID", from = String::class
                    ), OpenApiContent(
                        type = "EMAIL_ALREADY_EXIST", from = String::class
                    ), OpenApiContent(
                        type = "PSEUDO_IS_EMPTY", from = String::class
                    ), OpenApiContent(
                        type = "PSEUDO_TOO_SHORT", from = String::class
                    ), OpenApiContent(
                        type = "PSEUDO_TOO_LONG", from = String::class
                    ), OpenApiContent(
                        type = "PSEUDO_ALREADY_EXIST", from = String::class
                    ), OpenApiContent(
                        type = "PASSWORD_IS_EMPTY", from = String::class
                    ), OpenApiContent(
                        type = "PASSWORD_TOO_SHORT", from = String::class
                    )]
                ), OpenApiResponse(
                    "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                )]
            ) { ctx ->
                logger.info("USER_REGISTER")

                //verif mail
                if (ctx.formParam("email").isNullOrEmpty()) {
                    ctx.retour(400, "EMAIL_EMPTY")
                    //return@post
                } else if (!EmailValidator.isEmailValid(ctx.formParam("email").toString())) {
                    ctx.retour(400, "EMAIL_INVALID")
                    //return@post
                } else if (DatabaseConnect.AllUsers.findUserByEmail(ctx.formParam("email").toString()) != null) {
                    ctx.retour(400, "EMAIL_ALREADY_EXIST")
                    //return@post
                }

                //verif pseudo
                else if (ctx.formParam("pseudo").isNullOrEmpty()) {
                    ctx.retour(400, "PSEUDO_EMPTY")

                    //return@post
                } else if (ctx.formParam("pseudo").toString().length < 3) {
                    ctx.retour(400, "PSEUDO_TOO_SHORT")
                    //return@post
                } else if (ctx.formParam("pseudo").toString().length > 12) {
                    ctx.retour(400, "PSEUDO_TOO_LONG")
                    //return@post
                } else if (DatabaseConnect.AllUsers.findUserByPseudo(
                        ctx.formParam("pseudo").toString()
                    ) != null
                ) {
                    ctx.retour(400, "PSEUDO_ALREADY_EXIST")
                    //return@post
                }

                //verif password
                else if (ctx.formParam("password").isNullOrEmpty()) {
                    ctx.retour(400, "PASSWORD_EMPTY")
                    //return@post
                } else if (ctx.formParam("password").toString().length < 2) {
                    ctx.retour(400, "PASSWORD_TOO_SHORT")
                    //return@post
                } else {
                    val u = User(
                        ctx.formParam("email").toString(),
                        ctx.formParam("pseudo").toString(),
                        ctx.formParam("password").toString()
                    )
                    DatabaseConnect.AllUsers.addUser(u)
                    ctx.retour(201, Certification.create(u))
                }
            }

            //USER ROUTES
            path("User") {
                get("/") @OpenApi(
                    description = "Get user logged informations",
                    tags = ["User"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    responses = [OpenApiResponse(
                        "200", content = [OpenApiContent(type = "application/json", from = User::class)]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class)]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx ->
                    Certification.verification(ctx) { user ->
                        ctx.retour(200, user)
                    }
                }

                get("/{id}") @OpenApi(
                    description = "Get id user informations",
                    tags = ["User"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    pathParams = [OpenApiParam(name = "id", type = Int::class)],
                    responses = [OpenApiResponse(
                        "200", content = [OpenApiContent(type = "application/json", from = User::class)]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class)]
                    ), OpenApiResponse(
                        "404", content = [OpenApiContent(type = "USER_NOT_FOUND", from = String::class)]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx ->
                    Certification.verification(ctx) {
                        val datauser = DatabaseConnect.AllUsers.findUserById(ctx.pathParam("id").toInt())
                        if (datauser == null) {
                            ctx.retour(404, "USER_NOT_FOUND")
                            //return@verification
                        } else {
                            ctx.retour(200, datauser)
                        }
                    }
                }

                //modify user pseudo, email or password
                put("/") @OpenApi(
                    description = "Get id user informations",
                    tags = ["User"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    formParams = [
                        OpenApiFormParam(name = "email", type = String::class, required = false),
                        OpenApiFormParam(name = "pseudo", type = String::class, required = false),
                        OpenApiFormParam(name = "password", type = String::class, required = false)
                    ],
                    responses = [OpenApiResponse(
                        "200", content = [OpenApiContent(type = "token", from = String::class)]
                    ), OpenApiResponse(
                        "400", content = [
                            OpenApiContent(type = "PSEUDO_TOO_SHORT", from = String::class),
                            OpenApiContent(type = "PSEUDO_TOO_LONG", from = String::class),
                            OpenApiContent(type = "PSEUDO_ALREADY_EXIST", from = String::class),
                            OpenApiContent(type = "EMAIL_INVALID", from = String::class),
                            OpenApiContent(type = "EMAIL_ALREADY_EXIST", from = String::class),
                            OpenApiContent(type = "PASSWORD_TOO_SHORT", from = String::class),
                        ]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class)]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx ->
                    Certification.verification(ctx) { user ->
                        var email = ctx.formParam("email").toString()
                        var pseudo = ctx.formParam("pseudo").toString()
                        var password = ctx.formParam("password").toString()


                        if (pseudo.isEmpty()) {
                            pseudo = user.pseudo
                        } else {
                            if (pseudo.length < 3) {
                                ctx.retour(400, "PSEUDO_TOO_SHORT")
                                return@verification
                            }
                            if (pseudo.length > 12) {
                                ctx.retour(400, "PSEUDO_TOO_LONG")
                                return@verification
                            }
                            if (DatabaseConnect.AllUsers.findUserByPseudo(pseudo) != null) {
                                ctx.retour(400, "PSEUDO_ALREADY_EXIST")
                                return@verification
                            }
                        }

                        if (email.isEmpty()) {
                            email = user.email
                        } else {
                            if (!EmailValidator.isEmailValid(email)) {
                                ctx.retour(400, "EMAIL_INVALID")
                                return@verification
                            }
                            if (DatabaseConnect.AllUsers.findUserByEmail(email) != null) {
                                ctx.retour(400, "EMAIL_ALREADY_EXIST")
                                return@verification
                            }
                        }

                        if (password.isEmpty()) {
                            password = user.password
                        } else {
                            if (password.length < 2) {
                                ctx.retour(400, "PASSWORD_TOO_SHORT")
                                return@verification
                            }
                        }
                        user.pseudo = pseudo
                        user.email = email
                        user.password = password
                        Jsonbase.saveToJSON()
                        ctx.retour(200, Certification.create(user))
                    }

                }

                delete("/") @OpenApi(
                    description = "Delete user",
                    tags = ["User"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    responses = [OpenApiResponse(
                        "200", content = [OpenApiContent(type = "USER_DELETED", from = String::class)]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class)]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx ->
                    Certification.verification(ctx) { user ->
                       // DatabaseConnect.AllUsers.removeUser(user)
                        ctx.retour(200, "USER_DELETED")
                    }
                }
            }


            //Player
            path("/Player") {
                put("/position") @OpenApi(
                    description = "Update player position",
                    tags = ["Player"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    formParams = [
                        OpenApiFormParam(name = "posX", type = Int::class),
                        OpenApiFormParam(name = "posY", type = Int::class)
                    ],
                    responses = [OpenApiResponse(
                        "200", content = [OpenApiContent(type = "PLAYER_POSITION_UPDATED", from = String::class)]
                    ), OpenApiResponse(
                        "400",
                        content = [
                            OpenApiContent(type = "POSX_OR_POSY_IS_NOT_NUMBER", from = String::class),
                            OpenApiContent(type = "POSY_IS_NOT_VALID", from = String::class),
                            OpenApiContent(type = "POSX_IS_NOT_VALID", from = String::class),
                        ]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class)]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx ->
                    Certification.verification(ctx) { user ->

                        logger.info("PlayerPosition")
                        val x: Int
                        val y: Int

                        try {
                            x = ctx.formParam("posX")?.toInt() ?: -1
                            y = ctx.formParam("posY")?.toInt() ?: -1
                        } catch (e: Exception) {
                            ctx.retour(400, "POSX_OR_POSY_IS_NOT_NUMBER")
                            return@verification
                        }

                        if (y < 0 || y > 100 && !ctx.formParam("posY").isNullOrEmpty()) {
                            ctx.retour(400, "POSY_IS_NOT_VALID")
                            //return@verification
                        } else if (x < 0 || x > 100 && !ctx.formParam("posX").isNullOrEmpty()) {
                            ctx.retour(400, "POSX_IS_NOT_VALID")
                            //return@verification
                        } else {
                            user.player.position.x = x
                            user.player.position.y = y
                            ctx.retour(201, "POSITION_CHANGED")
                        }
                    }
                }

                get("/skin") @OpenApi(
                    description = "Get player skin",
                    tags = ["Player"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    responses = [OpenApiResponse(
                        "200", content = [OpenApiContent(type = "PLAYER_SKIN_FOUND", from = String::class)]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class)]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx ->
                    logger.info("GET_SKIN")
                    Certification.verification(ctx) { user ->
                        ctx.retour(200, user.player.skin)
                    }

                }

                put("/skin") @OpenApi(
                    description = "Change player skin",
                    tags = ["Player"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    formParams = [
                        OpenApiFormParam(name = "body", type = Int::class),
                        OpenApiFormParam(name = "eyes", type = Int::class),
                        OpenApiFormParam(name = "accessories", type = Int::class),
                        OpenApiFormParam(name = "hairstyle", type = Int::class),
                        OpenApiFormParam(name = "outfit", type = Int::class)
                    ],
                    responses = [OpenApiResponse(
                        "200", content = [OpenApiContent(type = "application/json", from = Skin::class)]
                    ), OpenApiResponse(
                        "400", content = [OpenApiContent(
                            type = "SKIN_IS_NOT_VALID", from = String::class
                        ), OpenApiContent(type = "SKIN_IS_NOT_FOUND", from = String::class)]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class)]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx ->
                    logger.info("CHANGE_SKIN_PLAYER")
                    Certification.verification(ctx) { user ->
                        user.player.skin.body = ctx.formParam("body")?.toInt() ?: user.player.skin.body
                        user.player.skin.eyes = ctx.formParam("eyes")?.toInt() ?: user.player.skin.eyes
                        user.player.skin.accessories =
                            ctx.formParam("accessories")?.toInt() ?: user.player.skin.accessories
                        user.player.skin.hairstyle = ctx.formParam("hairstyle")?.toInt() ?: user.player.skin.hairstyle
                        user.player.skin.outfit = ctx.formParam("outfit")?.toInt() ?: user.player.skin.outfit
                        ctx.retour(200, user.player.skin)
                    }
                }

                get("/guilds") @OpenApi(
                    description = "Get all Guild of the player",
                    tags = ["Player"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    responses = [OpenApiResponse(
                        "200", content = [OpenApiContent(
                            type = "application/json", from = Array<Guild>::class
                        ), OpenApiContent(type = "NO_GUILD", from = String::class)]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class)]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx ->
                    Certification.verification(ctx) { user ->
                        if (Jsonbase.allGuilds.findAllGuildByMember(user).isEmpty()) {
                            ctx.retour(200, "NO_GUILD")
                            //return@verification
                        } else {
                            ctx.retour(200, Jsonbase.allGuilds.findAllGuildByMember(user))
                        }
                    }
                }

                get("/owns") @OpenApi(
                    description = "Get all owned guilds of the player",
                    tags = ["Player"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    responses = [OpenApiResponse(
                        "200", content = [OpenApiContent(
                            type = "application/json", from = Array<Guild>::class
                        ), OpenApiContent(type = "NO_OWNED_GUILD", from = String::class)]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class)]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("GET_OWNER_GUILD")
                        if (Jsonbase.allGuilds.findAllGuildsByOwner(user).isEmpty()) {
                            ctx.retour(400, "NO_OWNED_GUILD")
                        } else {
                            ctx.retour(200, Jsonbase.allGuilds.findAllGuildsByOwner(user))
                        }
                    }
                }
            }


            //Guilde
            path("Guild") {
                get("/") @OpenApi(
                    description = "Get all guilds",
                    tags = ["Guild"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    responses = [OpenApiResponse(
                        "200", content = [OpenApiContent(
                            type = "application/json", from = Array<Guild>::class
                        )]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class)]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx ->
                    Certification.verification(ctx) {
                        logger.info("GET_ALL_GUILD")
                        ctx.retour(200, Jsonbase.allGuilds)
                    }
                }

                post("/") @OpenApi(
                    description = "Create a guild",
                    tags = ["Guild"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    formParams = [
                        OpenApiFormParam(name = "name", type = String::class),
                        OpenApiFormParam(name = "typeOfWork", type = Work::class),
                    ],
                    responses = [OpenApiResponse(
                        "200", content = [OpenApiContent(
                            type = "SUCCESS_CREATE_GUILD", from = String::class
                        )]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class)]
                    ), OpenApiResponse(
                        "400",
                        content = [OpenApiContent(type = "NAME_GUILD_REQUIRED", from = String::class), OpenApiContent(
                            type = "TYPE_WORK_REQUIRED", from = String::class
                        ), OpenApiContent(
                            type = "TYPE_WORK_NOT_EXIST",
                            from = String::class
                        ), OpenApiContent(type = "NAME_GUILD_ALREADY_EXIST", from = String::class)]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("POST_GUILD")
                        if (ctx.formParam("name").isNullOrEmpty()) {
                            ctx.retour(400, "NAME_GUILD_REQUIRED")
                            //return@verification
                        } else if (Jsonbase.allGuilds.findGuildByName(ctx.formParam("name").toString()) != null) {
                            ctx.retour(400, "NAME_GUILD_ALREADY_EXIST")
                            //return@verification
                        } else if (ctx.formParam("typeOfWork").isNullOrEmpty()) {
                            ctx.retour(400, "TYPE_WORK_REQUIRED")
                            //return@verification
                        } else if (!Work.validateWork(ctx.formParam("typeOfWork").toString())) {
                            ctx.retour(400, "TYPE_WORK_NOT_EXIST")
                            //return@verification
                        } else {
                            Jsonbase.allGuilds.addGuild(
                                Guild(
                                    ctx.formParam("name").toString(),
                                    user,
                                    Work.sendWork(ctx.formParam("typeOfWork").toString())
                                )
                            )
                            ctx.retour(200, "SUCCESS_CREATE_GUILD")
                        }
                    }
                }

                get("/{id}") @OpenApi(
                    description = "Get a guild",
                    tags = ["Guild"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    pathParams = [OpenApiParam(name = "id", type = String::class, required = true)],
                    responses = [OpenApiResponse(
                        "200", content = [OpenApiContent(
                            type = "application/json", from = Guild::class
                        )]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class)]
                    ), OpenApiResponse(
                        "404", content = [OpenApiContent(type = "GUILD_NOT_FOUND", from = String::class)]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx ->
                    Certification.verification(ctx) {
                        logger.info("GET_GUILD")
                        if (ctx.pathParam("id").isEmpty()) {
                            ctx.retour(400, "ID_GUILD_REQUIRED")
                            //return@verification
                        } else if (Jsonbase.allGuilds.findGuildById(ctx.pathParam("id").toInt()) == null) {
                            ctx.retour(404, "GUILD_NOT_FOUND")
                            //return@verification
                        } else {
                            ctx.retour(200, Jsonbase.allGuilds.findGuildById(ctx.pathParam("id").toInt())!!)
                        }
                    }
                }


                put("/{id}") @OpenApi(
                    description = "Update a guild",
                    tags = ["Guild"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    pathParams = [OpenApiParam(name = "id", type = String::class, required = true)],
                    formParams = [
                        OpenApiFormParam(name = "name", type = String::class),
                        OpenApiFormParam(name = "typeOfWork", type = Work::class),
                    ],
                    responses = [OpenApiResponse(
                        "200", content = [OpenApiContent(
                            type = "SUCCESS_UPDATE_GUILD", from = String::class
                        )]
                    ), OpenApiResponse(
                        "400", content = [
                            OpenApiContent(type = "ID_GUILD_REQUIRED", from = String::class),
                            OpenApiContent(type = "GUILD_ALREADY_EXIST", from = String::class),
                            OpenApiContent(type = "TYPE_WORK_NOT_EXIST", from = String::class),
                        ]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class)]
                    ), OpenApiResponse(
                        "404", content = [OpenApiContent(type = "GUILD_NOT_FOUND", from = String::class)]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx ->
                    Certification.verification(ctx) {
                        logger.info("PUT_GUILD")
                        var name = ctx.formParam("nameGuild") ?: ""
                        var work = ctx.formParam("typeWork") ?: ""

                        if (ctx.pathParam("id").isEmpty()) {
                            ctx.retour(400, "ID_GUILD_REQUIRED")
                            return@verification
                        }
                        val guild = Jsonbase.allGuilds.findGuildById(ctx.pathParam("id").toInt())
                        if (guild == null) {
                            ctx.retour(404, "GUILD_NOT_FOUND")
                            return@verification
                        }

                        if (name.isEmpty()) {
                            name = guild.name
                        } else {
                            if (Jsonbase.allGuilds.findGuildByName(name) != null) {
                                ctx.retour(400, "GUILD_ALREADY_EXIST")
                                return@verification
                            }
                        }

                        if (work.isEmpty()) {
                            work = guild.typeWork.name
                        } else {
                            if (!Work.validateWork(work)) {
                                ctx.retour(400, "TYPE_WORK_NOT_EXIST")
                                return@verification
                            }
                        }
                        guild.name = name
                        guild.typeWork = Work.sendWork(work)
                        ctx.retour(200, "SUCCESS_UPDATE_GUILD")
                    }
                }

                delete("/{id}") @OpenApi(
                    description = "Delete guild",
                    tags = ["Guild"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    pathParams = [OpenApiParam(name = "id", type = String::class, required = true)],
                    responses = [OpenApiResponse(
                        "200", content = [OpenApiContent(
                            type = "SUCCESS_DELETE_GUILD", from = String::class
                        )]
                    ), OpenApiResponse(
                        "400", content = [
                            OpenApiContent(type = "ID_GUILD_REQUIRED", from = String::class)
                        ]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class)]
                    ), OpenApiResponse(
                        "404", content = [OpenApiContent(type = "GUILD_NOT_FOUND", from = String::class)]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("DELETE_GUILD")
                        if (ctx.pathParam("id").isEmpty()) {
                            ctx.retour(400, "ID_GUILD_REQUIRED")
                            //return@verification
                        } else if (Jsonbase.allGuilds.findGuildById(ctx.pathParam("id").toInt()) == null) {
                            ctx.retour(404, "GUILD_NOT_FOUND")
                            //return@verification
                        } else if (!Jsonbase.allGuilds.findAllGuildsByOwner(user)
                                .contains(Jsonbase.allGuilds.findGuildById(ctx.pathParam("id").toInt()))
                        ) {
                            ctx.retour(400, "GUILD_NOT_OWNER")
                        } else {
                            Jsonbase.allGuilds.remove(Jsonbase.allGuilds.findGuildById(ctx.pathParam("id").toInt()))
                            ctx.retour(200, "SUCCESS_DELETE_GUILD")
                        }
                    }
                }

                patch("/{id}/join") @OpenApi(
                    description = "Join Waiting List of guild by id",
                    tags = ["Guild"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    pathParams = [OpenApiParam(name = "id", type = String::class, required = true)],
                    responses = [OpenApiResponse(
                        "200", content = [OpenApiContent(
                            type = "SUCCESS_JOIN_WAITING_LIST", from = String::class
                        )]
                    ), OpenApiResponse(
                        "400", content = [
                            OpenApiContent(type = "ID_GUILD_REQUIRED", from = String::class),
                            OpenApiContent(type = "PLAYER_ALREADY_IN_GUILD", from = String::class),
                            OpenApiContent(type = "PLAYER_ALREADY_IN_WAITING_LIST", from = String::class)
                        ]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class)]
                    ), OpenApiResponse(
                        "404", content = [OpenApiContent(type = "GUILD_NOT_FOUND", from = String::class)]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("PlayerJoinGuild")
                        if (ctx.pathParam("id").isEmpty()) {
                            ctx.retour(400, "ID_GUILD_REQUIRED")
                            //return@verification
                        } else if (Jsonbase.allGuilds.findGuildById(ctx.pathParam("id").toInt()) == null) {
                            ctx.retour(404, "GUILD_NOT_FOUND")
                            //return@verification
                        } else if (Jsonbase.allGuilds.findGuildById(ctx.pathParam("id").toInt())!!.employees.contains(
                                user
                            )
                        ) {
                            ctx.retour(400, "PLAYER_ALREADY_IN_GUILD")
                            //return@verification
                        } else if (Jsonbase.allGuilds.findGuildById(ctx.pathParam("id").toInt())!!.waitingList.contains(
                                user
                            )
                        ) {
                            ctx.retour(400, "PLAYER_ALREADY_IN_WAITING_LIST")
                            //return@verification
                        } else {
                            Jsonbase.allGuilds.findGuildById(ctx.pathParam("id").toInt())?.AddMember(user)
                            ctx.retour(200, "SUCCESS_JOIN_WAITING_LIST")
                        }
                    }
                }

                patch("/{id}/leave") @OpenApi(
                    description = "Leave employees of guild by id",
                    tags = ["Guild"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    pathParams = [OpenApiParam(name = "id", type = String::class, required = true)],
                    responses = [OpenApiResponse(
                        "200", content = [OpenApiContent(
                            type = "SUCCESS_LEAVE_GUILD", from = String::class
                        )]
                    ), OpenApiResponse(
                        "400", content = [
                            OpenApiContent(type = "ID_GUILD_REQUIRED", from = String::class),
                            OpenApiContent(type = "PLAYER_NOT_IN_GUILD_OR_IS_OWNER", from = String::class)
                        ]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class)]
                    ), OpenApiResponse(
                        "404", content = [OpenApiContent(type = "GUILD_NOT_FOUND", from = String::class)]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("PlayerLeaveGuild")
                        if (ctx.pathParam("id").isEmpty()) {
                            ctx.retour(400, "ID_GUILD_REQUIRED")
                            return@verification
                        } else if (Jsonbase.allGuilds.findGuildById(ctx.pathParam("id").toInt()) == null) {
                            ctx.retour(404, "GUILD_NOT_FOUND")
                            return@verification
                        } else if (Jsonbase.allGuilds.findGuildById(ctx.pathParam("id").toInt())
                                ?.RemoveMember(user) != true
                        ) {
                            ctx.retour(400, "PLAYER_NOT_IN_GUILD_OR_IS_OWNER")
                        } else {
                            ctx.retour(200, "SUCCESS_LEAVE_GUILD")
                        }
                    }
                }

                get("/{id}/owner") @OpenApi(
                    description = "Get owner of guild by id",
                    tags = ["Guild"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    pathParams = [OpenApiParam(name = "id", type = String::class, required = true)],
                    responses = [OpenApiResponse(
                        "200", content = [OpenApiContent(
                            type = "application/json", from = User::class
                        )]
                    ), OpenApiResponse(
                        "400", content = [
                            OpenApiContent(type = "ID_GUILD_REQUIRED", from = String::class),
                        ]
                    ), OpenApiResponse(
                        "404", content = [OpenApiContent(type = "GUILD_NOT_FOUND", from = String::class)]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class)]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx ->
                    Certification.verification(ctx) {
                        logger.info("OwnerGuild")
                        if (ctx.pathParam("id").isEmpty()) {
                            ctx.retour(400, "ID_GUILD_REQUIRED")
                            //return@verification
                        } else if (Jsonbase.allGuilds.findGuildById(ctx.pathParam("id").toInt()) == null) {
                            ctx.retour(404, "GUILD_NOT_FOUND")
                        } else {
                            Jsonbase.allGuilds.findGuildById(ctx.pathParam("id").toInt())
                                ?.let { ctx.retour(200, it.owner) }
                        }
                    }
                }

                get("/{id}/members") @OpenApi(
                    description = "Get list of guild member by id",
                    tags = ["Guild"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    pathParams = [OpenApiParam(name = "id", type = String::class, required = true)],
                    responses = [OpenApiResponse(
                        "200", content = [OpenApiContent(
                            type = "application/json", from = Array<User>::class
                        )]
                    ), OpenApiResponse(
                        "400", content = [
                            OpenApiContent(type = "ID_GUILD_REQUIRED", from = String::class),
                        ]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class)]
                    ), OpenApiResponse(
                        "404", content = [OpenApiContent(type = "GUILD_NOT_FOUND", from = String::class)]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx ->
                    Certification.verification(ctx) {
                        logger.info("GET_ALL_MEMBERS")
                        if (ctx.pathParam("id").isEmpty()) {
                            ctx.retour(400, "ID_GUILD_REQUIRED")
                            //return@verification
                        } else {
                            val dataguild = Jsonbase.allGuilds.findGuildById(ctx.pathParam("id").toInt())
                            if (dataguild == null) {
                                ctx.retour(404, "GUIlD_NOT_FOUND")
                                //return@verification
                            } else {
                                ctx.retour(200, dataguild.employees)
                            }
                        }
                    }
                }


                get("/{id}/waiting") @OpenApi(
                    description = "Get list of guild member waiting for validation by id",
                    tags = ["Guild"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    pathParams = [OpenApiParam(name = "id", type = String::class, required = true)],
                    responses = [OpenApiResponse(
                        "200", content = [OpenApiContent(
                            type = "waiting list", from = Array<User>::class
                        )]
                    ), OpenApiResponse(
                        "400", content = [
                            OpenApiContent(type = "ID_GUILD_REQUIRED", from = String::class),
                        ]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class),
                            OpenApiContent(type = "PLAYER_NOT_OWNER_GUILD", from = String::class)
                        ]
                    ), OpenApiResponse(
                        "404", content = [OpenApiContent(type = "GUILD_NOT_FOUND", from = String::class)]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("GET_WAITING_LIST")
                        if (ctx.pathParam("id").isEmpty()) {
                            ctx.retour(400, "ID_GUILD_REQUIRED")
                            //return@verification
                        } else {
                            val dataguild = Jsonbase.allGuilds.findGuildById(ctx.pathParam("id").toInt())
                            if (dataguild == null) {
                                ctx.retour(404, "GUILD_NOT_EXIST")
                                //return@verification
                            } else if (dataguild.owner != user) {
                                ctx.retour(403, "PLAYER_NOT_OWNER_GUILD")
                                //return@verification
                            } else {
                                ctx.retour(200, dataguild.waitingList)
                            }
                        }
                    }
                }

                patch("/{id}/accept/{playerId}") @OpenApi(
                    description = "Accept a player in guild by id",
                    tags = ["Guild"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    pathParams = [OpenApiParam(
                        name = "id",
                        type = String::class,
                        required = true
                    ), OpenApiParam(name = "playerId", type = String::class, required = true)],
                    responses = [OpenApiResponse(
                        "200", content = [OpenApiContent(
                            type = "SUCCESS_ACCEPT_MEMBER", from = String::class
                        )]
                    ), OpenApiResponse(
                        "400", content = [
                            OpenApiContent(type = "ID_GUILD_REQUIRED", from = String::class),
                            OpenApiContent(type = "ID_PLAYER_REQUIRED", from = String::class),
                        ]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class),
                            OpenApiContent(type = "PLAYER_NOT_OWNER_GUILD", from = String::class)
                        ]
                    ), OpenApiResponse(
                        "404", content = [
                            OpenApiContent(type = "GUILD_NOT_FOUND", from = String::class),
                            OpenApiContent(type = "PLAYER_NOT_FOUND", from = String::class),
                            OpenApiContent(type = "PLAYER_NOT_WAITING_LIST", from = String::class),
                        ]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("ACCEPT_MEMBER")
                        if (ctx.pathParam("id").isEmpty()) {
                            ctx.retour(400, "ID_GUILD_REQUIRED")
                            //return@verification
                        } else if (ctx.pathParam("playerId").isEmpty()) {
                            ctx.retour(400, "ID_PLAYER_REQUIRED")
                            //return@verification
                        } else {
                            val datauser = DatabaseConnect.AllUsers.findUserById(ctx.pathParam("playerId").toInt())
                            if (datauser == null) {
                                ctx.retour(404, "PLAYER_NOT_FOUND")
                                //return@verification
                            } else {
                                val dataguild = Jsonbase.allGuilds.findGuildById(ctx.pathParam("id").toInt())
                                if (dataguild == null) {
                                    ctx.retour(404, "GUILD_NOT_FOUND")
                                    //return@verification
                                } else if (dataguild.owner != user) {
                                    ctx.retour(403, "PLAYER_NOT_OWNER_GUILD")
                                    //return@verification
                                } else if (!dataguild.waitingList.contains(datauser)) {
                                    ctx.retour(404, "PLAYER_NOT_WAITING_LIST")
                                    //return@verification
                                } else {
                                    dataguild.AddMember(datauser)
                                    dataguild.waitingList.remove(datauser)
                                    ctx.retour(200, "SUCCESS_ACCEPT_MEMBER")
                                }
                            }
                        }
                    }
                }

                patch("/{id}/decline/{playerId}") @OpenApi(
                    description = "Decline a player in a guild",
                    tags = ["Guild"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    pathParams = [OpenApiParam(
                        name = "id",
                        type = String::class,
                        required = true
                    ), OpenApiParam(name = "playerId", type = String::class, required = true)],
                    responses = [
                        OpenApiResponse(
                            "200", content = [
                                OpenApiContent(type = "SUCCESS_REFUSE_MEMBER", from = String::class)
                            ]
                        ), OpenApiResponse(
                            "400", content = [
                                OpenApiContent(type = "ID_GUILD_REQUIRED", from = String::class),
                                OpenApiContent(type = "ID_PLAYER_REQUIRED", from = String::class)
                            ]
                        ), OpenApiResponse(
                            "403", content = [
                                OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                                OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class),
                                OpenApiContent(type = "PLAYER_NOT_OWNER_GUILD", from = String::class)
                            ]
                        ), OpenApiResponse(
                            "404", content = [
                                OpenApiContent(type = "GUILD_NOT_FOUND", from = String::class),
                                OpenApiContent(type = "PLAYER_NOT_FOUND", from = String::class),
                                OpenApiContent(type = "PLAYER_NOT_WAITING_LIST", from = String::class),
                            ]
                        ), OpenApiResponse(
                            "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                        )]
                ) { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("REFUSE_MEMBER")
                        if (ctx.pathParam("id").isEmpty()) {
                            ctx.retour(400, "ID_GUILD_REQUIRED")
                            //return@verification
                        } else if (ctx.pathParam("playerId").isEmpty()) {
                            ctx.retour(400, "ID_PLAYER_REQUIRED")
                            //return@verification
                        } else {
                            val datauser = DatabaseConnect.AllUsers.findUserById(ctx.pathParam("playerId").toInt())
                            if (datauser == null) {
                                ctx.retour(400, "PLAYER_NOT_FOUND")
                                //return@verification
                            } else {
                                val dataguild = Jsonbase.allGuilds.findGuildById(ctx.pathParam("id").toInt())
                                if (dataguild == null) {
                                    ctx.retour(404, "GUI_NOT_FOUND")
                                    //return@verification
                                } else if (dataguild.owner != user) {
                                    ctx.retour(403, "PLAYER_NOT_OWNER_GUILD")
                                    //return@verification
                                } else if (!dataguild.waitingList.contains(datauser)) {
                                    ctx.retour(404, "PLAYER_NOT_WAITING_LIST")
                                    return@verification
                                } else {
                                    dataguild.waitingList.remove(datauser)
                                    ctx.retour(200, "SUCCESS_REFUSE_MEMBER")
                                }
                            }
                        }
                    }
                }

                patch("/{id}/kick/{playerId}") @OpenApi(
                    description = "Kick a player from the guild",
                    tags = ["Guild"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    pathParams = [OpenApiParam(
                        name = "id",
                        type = String::class,
                        required = true
                    ), OpenApiParam(name = "playerId", type = String::class, required = true)],
                    responses = [
                        OpenApiResponse(
                            "200", content = [
                                OpenApiContent(type = "SUCCESS_KICK_MEMBER", from = String::class)
                            ]
                        ), OpenApiResponse(
                            "400", content = [
                                OpenApiContent(type = "ID_GUILD_REQUIRED", from = String::class),
                                OpenApiContent(type = "ID_PLAYER_REQUIRED", from = String::class)
                            ]
                        ), OpenApiResponse(
                            "403", content = [
                                OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                                OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class),
                                OpenApiContent(type = "PLAYER_NOT_OWNER_GUILD", from = String::class)
                            ]
                        ), OpenApiResponse(
                            "404", content = [
                                OpenApiContent(type = "GUILD_NOT_FOUND", from = String::class),
                                OpenApiContent(type = "PLAYER_NOT_FOUND", from = String::class),
                                OpenApiContent(type = "PLAYER_NOT_MEMBER_GUILD", from = String::class),
                            ]
                        ), OpenApiResponse(
                            "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                        )]
                ) { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("KICK_MEMBER")
                        if (ctx.pathParam("id").isEmpty()) {
                            ctx.retour(400, "ID_GUILD_REQUIRED")
                            //return@verification
                        } else if (ctx.pathParam("playerId").isEmpty()) {
                            ctx.retour(400, "ID_PLAYER_REQUIRED")
                            //return@verification
                        } else {
                            val datauser = DatabaseConnect.AllUsers.findUserById(ctx.pathParam("playerId").toInt())
                            if (datauser == null) {
                                ctx.retour(404, "PLAYER_NOT_FOUND")
                                //return@verification
                            } else {
                                val dataguild = Jsonbase.allGuilds.findGuildById(ctx.pathParam("id").toInt())
                                if (dataguild == null) {
                                    ctx.retour(404, "GUI_NOT_FOUND")
                                    //return@verification
                                } else if (dataguild.owner != user) {
                                    ctx.retour(403, "PLAYER_NOT_OWNER_GUILD")
                                    //return@verification
                                } else if (!dataguild.employees.contains(datauser)) {
                                    ctx.retour(404, "PLAYER_NOT_MEMBER_GUILD")
                                } else {
                                    dataguild.employees.remove(datauser)
                                    ctx.retour(200, "SUCCESS_KICK_MEMBER")
                                }
                            }
                        }
                    }
                }

            }

            //Chat
            path("/Chat") {
                get("/") @OpenApi(
                    description = "Get all chat since 5 minutes",
                    tags = ["Chat"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    responses = [OpenApiResponse(
                        "200", content = [OpenApiContent(type = "application/json", from = Array<Message>::class)]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class)
                        ]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx -> //List last message from 5 minutes
                    Certification.verification(ctx) {
                        logger.info("GET_ALL_CHAT")
                        ctx.retour(200, Jsonbase.allChat.getMessagesBeetweenDate(Date().time - 300000, Date().time))
                    }
                }

                post("/create") @OpenApi(
                    description = "Create a new message",
                    tags = ["Chat"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    formParams = [
                        OpenApiFormParam(name = "message", type = String::class)
                    ],
                    responses = [OpenApiResponse(
                        "200", content = [OpenApiContent(type = "SUCCESS_CREATE_MESSAGE_CHAT", from = String::class)]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class)
                        ]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("CREATE_MESSAGE_CHAT")
                        val message = ctx.formParam("message")
                        if (message.isNullOrEmpty()) {
                            ctx.retour(400, "MESSAGE_REQUIRED")
                            //return@verification
                        } else {
                            Jsonbase.allChat.addMessage(Message(message, user, Date().time))
                            ctx.retour(200, "SUCCESS_CREATE_MESSAGE_CHAT")
                        }
                    }
                }

                get("/all") @OpenApi(
                    description = "Get all chat",
                    tags = ["Chat"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    responses = [OpenApiResponse(
                        "200", content = [OpenApiContent(type = "application/json", from = Array<Message>::class)]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class)
                        ]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx ->
                    Certification.verification(ctx) {
                        logger.info("GET_ALL_CHAT")
                        ctx.retour(200, Jsonbase.allChat)
                    }
                }
            }


            //MAP
            path("/Map") {
                get("/") @OpenApi(
                    description = "Get Current map for user",
                    tags = ["Map"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    responses = [OpenApiResponse(
                        "200", content = [OpenApiContent(type = "application/json", from = Map::class)]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class)
                        ]
                    ), OpenApiResponse(
                        "404", content = [OpenApiContent(type = "PLAYER_IS_NOT_IN_ANY_MAP", from = String::class)]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("GET_CURRENT_MAP_FOR_USER")
                        val current = Jsonbase.allGuilds.findGuildById(user.player.currentGuildMap)
                        if (current == null) {
                            ctx.retour(404, "PLAYER_IS_NOT_IN_ANY_MAP")
                            //return@verification
                        } else {
                            ctx.retour(200, current.place)
                        }
                    }
                }

                patch("{id}/go") @OpenApi(
                    description = "Go to map",
                    tags = ["Map"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    pathParams = [OpenApiParam(name = "id", type = Int::class, required = true)],
                    responses = [OpenApiResponse(
                        "200", content = [
                            OpenApiContent(type = "SUCCESS_GO_TO_MAP", from = String::class),
                            OpenApiContent(type = "PLAYER_IS_ALREADY_IN_MAP", from = String::class),
                        ]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class)
                        ]
                    ), OpenApiResponse(
                        "404", content = [OpenApiContent(type = "GUILD_MAP_NOT_EXIST", from = String::class)]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("GO_TO_MAP")
                        val id = ctx.pathParam("id").toInt()
                        val guild = Jsonbase.allGuilds.findGuildById(id)
                        if (guild == null) {
                            ctx.retour(404, "GUILD_MAP_NOT_EXIST")
                            //return@verification
                        } else if (user.player.currentGuildMap == id) {
                            ctx.retour(200, "PLAYER_IS_ALREADY_IN_MAP")
                            //return@verification
                        } else {
                            user.player.currentGuildMap = id
                            ctx.retour(200, "SUCCESS_GO_TO_MAP")
                        }
                    }
                }

                patch("quit") @OpenApi(
                    description = "Quit map",
                    tags = ["Map"],
                    headers = [OpenApiParam(name = "token", type = String::class, required = true)],
                    responses = [OpenApiResponse(
                        "200", content = [
                            OpenApiContent(type = "SUCCESS_QUIT_MAP", from = String::class)
                        ]
                    ), OpenApiResponse(
                        "403", content = [
                            OpenApiContent(type = "DECODED_BUT_UNKNOW_PLAYER", from = String::class),
                            OpenApiContent(type = "UNDECODED_JWT_TOKEN", from = String::class)
                        ]
                    ), OpenApiResponse(
                        "404", content = [OpenApiContent(type = "PLAYER_IS_NOT_IN_ANY_MAP", from = String::class)]
                    ), OpenApiResponse(
                        "500", content = [OpenApiContent(type = "SERVER_ERROR", from = String::class)]
                    )]
                ) { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("QUIT_MAP")
                        if (user.player.currentGuildMap == -1) {
                            ctx.retour(404, "PLAYER_IS_NOT_IN_ANY_MAP")
                            //return@verification
                        } else {
                            user.player.currentGuildMap = -1
                            ctx.retour(200, "SUCCESS_QUIT_MAP")
                        }
                    }
                }
            }
        }

        logger.info("Find redoc : http://localhost:7000/redoc")
        logger.info("Find swagger : http://localhost:7000/swagger")
    }
}
