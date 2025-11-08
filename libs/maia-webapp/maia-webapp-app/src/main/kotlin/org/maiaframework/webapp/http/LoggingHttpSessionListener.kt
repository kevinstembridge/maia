package org.maiaframework.webapp.http

import org.maiaframework.common.logging.getLogger
import jakarta.servlet.http.HttpSessionEvent
import jakarta.servlet.http.HttpSessionListener

class LoggingHttpSessionListener: HttpSessionListener {


    private val logger = getLogger<LoggingHttpSessionListener>()


    override fun sessionCreated(se: HttpSessionEvent) {
        logger.info("Session created: ${se.session.id}")
    }


    override fun sessionDestroyed(se: HttpSessionEvent) {
        logger.info("Session destroyed: ${se.session.id}")
    }


}
