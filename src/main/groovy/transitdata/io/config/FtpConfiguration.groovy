package transitdata.io.config

import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.Environment
import transitdata.io.batch.FileUnzipperTasklet
import transitdata.io.batch.FtpGetRemoteFilesTasklet

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory
import org.springframework.integration.file.remote.session.SessionFactory

@Configuration
@PropertySource('classpath:ftp.properties')
class FtpConfiguration {

    @Autowired
    FileUnzipperTasklet fileUnzipperTasklet

    @Autowired
    Environment env

    @Bean
    SessionFactory ftpClientFactory() {
        DefaultFtpSessionFactory ftpSessionFactory = new DefaultFtpSessionFactory()
        ftpSessionFactory.with {
            host = env.getProperty('batch.ftp.hostname')
            port = env.getProperty('batch.ftp.port', Integer, 21)
            username = env.getProperty('batch.ftp.username', 'anonymous')
            password = env.getProperty('batch.ftp.password', 'anonymous')
        }

        return ftpSessionFactory
    }

    @Bean
    @Scope(value='step')
    FtpGetRemoteFilesTasklet ftpGetRemoteFilesTasklet() {
        FtpGetRemoteFilesTasklet ftpTasklet = new FtpGetRemoteFilesTasklet()
        ftpTasklet.with {
            retryIfNotFound = true
            downloadFileAttempts = env.getProperty('batch.ftp.pull.retryAttempts', Integer, 3)
            retryIntervalMilliseconds = env.getProperty('batch.ftp.pull.retryDelayMs', Integer, 10000)
            fileNamePattern = env.getProperty('batch.ftp.pull.local.filename', 'google_transit.zip')
            remoteDirectory = env.getProperty('batch.ftp.pull.remote.directory', '/')
            localDirectory = new File(env.getProperty('batch.ftp.pull.local.directory', 'gtfs'))
            sessionFactory = ftpClientFactory()
        }

        return ftpTasklet
    }

    @Bean
    @Scope(value='step')
    FileUnzipperTasklet fileUnzipperTasklet() {
        return fileUnzipperTasklet
    }

}
