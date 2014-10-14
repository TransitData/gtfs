package transitdata.io

import com.jolbox.bonecp.BoneCPDataSource
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.StepScopeTestExecutionListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import spock.lang.Specification
import transitdata.io.domain.Agency

@ContextConfiguration('/agency-job.xml')
@TestExecutionListeners([
        DependencyInjectionTestExecutionListener,
        StepScopeTestExecutionListener
])
class AgencyJobIntegrationSpec extends Specification {

    @Autowired
    JobLauncherTestUtils jobLauncher

    @Autowired
    BoneCPDataSource dataSource

    JdbcTemplate jdbcTemplate

    def setup() {
        jdbcTemplate = new JdbcTemplate(dataSource)
    }

    void 'simple properties injection'() {
        expect:
        jobLauncher != null
    }

    void 'launch job'() {
        given:
        Agency expectedAgency = new Agency(
                agency_id: '0',
                agency_name: 'Metro Transit',
                agency_url: 'http://www.metrotransit.org',
                agency_timezone: 'America/Chicago',
                agency_lang: 'EN'
        )

        when: 'job is run'
        def jobExecution = jobLauncher.launchJob()
        List<Agency> agencyList = jdbcTemplate.queryForList('select * from agency')

        then: 'it completes successfully (execution is synchronous, so we don\'t have to wait for completion)'
        jobExecution.exitStatus == ExitStatus.COMPLETED

        and:
        agencyList.size() == 16
        agencyList.first().agency_id == expectedAgency.agency_id
        agencyList.first().agency_url == expectedAgency.agency_url
        agencyList.first().agency_lang == expectedAgency.agency_lang
    }
}
