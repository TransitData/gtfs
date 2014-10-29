package transitdata.io.batch

import com.jolbox.bonecp.BoneCPDataSource
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.StepScopeTestExecutionListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import spock.lang.Specification

@ContextConfiguration('/jobs/ftp-job.xml')
@TestExecutionListeners([
        DependencyInjectionTestExecutionListener,
        StepScopeTestExecutionListener
])
class FtpJobIntegrationSpec extends Specification {

    @Autowired
    JobLauncherTestUtils jobLauncher

    @Autowired
    BoneCPDataSource dataSource

    JdbcTemplate jdbcTemplate

    void 'simple properties injection'() {
        expect:
        jobLauncher != null
    }

    // I can't figure out how to test the FTP transfer.
    // perhaps we could use http://mockftpserver.sourceforge.net/index.html
    void 'launch job'() {
        expect:
        true
    }
}
