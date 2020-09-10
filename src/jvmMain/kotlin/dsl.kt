import io.ktor.application.*
import io.ktor.sessions.*
import io.ktor.util.pipeline.*

data class RequireDSLInternal(val value: Boolean)

inline fun PipelineContext<*, ApplicationCall>.requireSession(block: (session: LoginSession) -> Unit): RequireDSLInternal {
    val session = call.sessions.get<LoginSession>()
    if (session != null) {
        block(session)
        return RequireDSLInternal(true)
    }

    return RequireDSLInternal(false)
}

inline infix fun RequireDSLInternal.or(block: () -> Unit) {
    if (!value) {
        block()
    }
}
