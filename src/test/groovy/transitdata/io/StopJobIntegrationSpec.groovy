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
import transitdata.io.domain.Stop

@ContextConfiguration('/test-config.xml')
@TestExecutionListeners([
        DependencyInjectionTestExecutionListener,
        StepScopeTestExecutionListener
])
class StopJobIntegrationSpec extends Specification {

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
        Stop expectedStop = new Stop(
                stop_id: '1000',
                stop_name: '50th St W & Upton Ave S',
                stop_desc: 'Near side E',
                stop_lat: 44.912365,
                stop_lon: '-93.315178',
                stop_street: '50th St W',
                stop_city: 'MINNEAPOLIS'
        )

        when: 'job is run'
        def jobExecution = jobLauncher.launchJob()
        List<Stop> stopList = jdbcTemplate.queryForList('select * from stop')

        then: 'it completes successfully (execution is synchronous, so we don\'t have to wait for completion)'
        jobExecution.exitStatus == ExitStatus.COMPLETED

        and:
        stopList.size() == 7
        stopList.first().stop_id == expectedStop.stop_id
        stopList.first().stop_name == expectedStop.stop_name
        stopList.first().stop_city == expectedStop.stop_city
    }

}
