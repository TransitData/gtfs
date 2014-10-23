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
import transitdata.io.domain.Trip

@ContextConfiguration('/jobs/trip-job.xml')
@TestExecutionListeners([
        DependencyInjectionTestExecutionListener,
        StepScopeTestExecutionListener
])
class TripJobIntegrationSpec extends Specification {

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
        Trip expectedTrip = new Trip(
                transit_system: 'METRO_TRANSIT',
                route_id: '766-74',
                service_id: 'AUG14-MVS-BUS-Weekday-01',
                trip_id: '7672288-AUG14-MVS-BUS-Weekday-01',
                trip_headsign: 'Northbound 766G Expess/Richardson PR/Via W River Rd',
                trip_short_name: null,
                direction_id: null,
                block_id: '2070',
                shape_id: '7660002',
                wheelchair_accessible: 1,
                bikes_allowed: null
        )

        when: 'job is run'
        def jobExecution = jobLauncher.launchJob()
        List<Trip> tripList = jdbcTemplate.queryForList('select * from trip')

        then: 'it completes successfully (execution is synchronous, so we don\'t have to wait for completion)'
        jobExecution.exitStatus == ExitStatus.COMPLETED

        and:
        tripList.size() == 11
        tripList.first().transit_system == expectedTrip.transit_system
        tripList.first().route_id == expectedTrip.route_id
        tripList.first().service_id == expectedTrip.service_id
        tripList.first().trip_id == expectedTrip.trip_id
        tripList.first().trip_headsign == expectedTrip.trip_headsign
        tripList.first().trip_short_name == expectedTrip.trip_short_name
        tripList.first().direction_id == expectedTrip.direction_id
        tripList.first().block_id == expectedTrip.block_id
        tripList.first().shape_id == expectedTrip.shape_id
        tripList.first().wheelchair_accessible == expectedTrip.wheelchair_accessible
        tripList.first().bikes_allowed == expectedTrip.bikes_allowed
    }

}
