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
import transitdata.io.domain.Route

@ContextConfiguration('/jobs/routes-job.xml')
@TestExecutionListeners([
        DependencyInjectionTestExecutionListener,
        StepScopeTestExecutionListener
])
class RouteJobIntegrationSpec extends Specification {

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
        Route expectedRoute = new Route(
                route_id : '2-74',
                agency_id : '0',
                route_short_name : '2',
                route_long_name : 'Franklin Av - Riverside Av - U of M - 8th St SE',
                route_desc : '',
                route_type : '3',
                route_url : 'http://www.metrotransit.org/route/2',
                route_color : '',
                route_text_color : '000000'
        )

        when: 'job is run'
        def jobExecution = jobLauncher.launchJob()
        List<Route> routeList = jdbcTemplate.queryForList('select * from route')

        then: 'it completes successfully (execution is synchronous, so we don\'t have to wait for completion)'
        jobExecution.exitStatus == ExitStatus.COMPLETED

        and:
        routeList.size() == 23
        routeList.first().route_id == expectedRoute.route_id
        routeList.first().agency_id == expectedRoute.agency_id
        routeList.first().route_short_name == expectedRoute.route_short_name
        routeList.first().route_long_name == expectedRoute.route_long_name
        routeList.first().route_desc == expectedRoute.route_desc
        routeList.first().route_type == expectedRoute.route_type
        routeList.first().route_url == expectedRoute.route_url
        routeList.first().route_color == expectedRoute.route_color
        routeList.first().route_text_color == expectedRoute.route_text_color

    }
}
