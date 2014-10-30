package transitdata.io.batch

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.jolbox.bonecp.BoneCPDataSource
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.StepScopeTestExecutionListener
import org.springframework.core.env.Environment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import spock.lang.Specification

@ContextConfiguration('/jobs/file-unzipper-job.xml')
@TestExecutionListeners([
        DependencyInjectionTestExecutionListener,
        StepScopeTestExecutionListener
])
class FileUnzipperIntegrationSpec extends Specification {

    private static final Logger log = LoggerFactory.getLogger(FileUnzipperIntegrationSpec)

    def files = ['agency.txt',
                    'calendar.txt',
                    'calendar_dates.txt',
                    'routes.txt',
                    'shapes.txt',
                    'stop_times.txt',
                    'stops.txt',
                    'trips.txt']

    @Autowired
    JobLauncherTestUtils jobLauncher

    @Autowired
    BoneCPDataSource dataSource

    @Autowired
    Environment env

    JdbcTemplate jdbcTemplate

    def setup() {
        jdbcTemplate = new JdbcTemplate(dataSource)
    }

    def cleanup() {
        // delete all extracted files
        log.info('cleaning up all extracted test files')
        files.each { fileName ->
            File file = new File(env.getProperty('batch.ftp.pull.local.directory') + '/' + fileName)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    void 'simple properties injection'() {
        expect:
        jobLauncher != null
    }

    void 'launch job'() {

        when: 'job is run'
        def jobExecution = jobLauncher.launchJob()

        then: 'it completes successfully (execution is synchronous, so we don\'t have to wait for completion)'
        jobExecution.exitStatus == ExitStatus.COMPLETED

        and:

        files.each { fileName ->
            new File(env.getProperty('batch.ftp.pull.local.directory') + '/' + fileName).exists()
            log.info("$fileName extracted succesfully...")
        }
    }

}
