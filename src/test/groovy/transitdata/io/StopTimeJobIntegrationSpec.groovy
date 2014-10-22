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
import transitdata.io.domain.StopTime

@ContextConfiguration('/jobs/stoptime-job.xml')
@TestExecutionListeners([
        DependencyInjectionTestExecutionListener,
        StepScopeTestExecutionListener
])
class StopTimeJobIntegrationSpec extends Specification {

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
        StopTime expectedStopTime = new StopTime(
                transit_system: 'METRO_TRANSIT',
                trip_id: '7672288-AUG14-MVS-BUS-Weekday-01',
                arrival_time: '17:13:00',
                departure_time: '17:13:00',
                stop_id: '49358',
                stop_sequence: '1',
                stop_headsign: null,
                pickup_type: '1',
                drop_off_type: '1',
                shape_dist_traveled: null
        )

        when: 'job is run'
        def jobExecution = jobLauncher.launchJob()
        List<StopTime> stopTimeList = jdbcTemplate.queryForList('select * from stop_time')

        then: 'it completes successfully (execution is synchronous, so we don\'t have to wait for completion)'
        jobExecution.exitStatus == ExitStatus.COMPLETED

        and:
        stopTimeList.size() == 20
        stopTimeList.first().transit_system == expectedStopTime.transit_system
        stopTimeList.first().trip_id == expectedStopTime.trip_id
        stopTimeList.first().arrival_time == expectedStopTime.arrival_time
        stopTimeList.first().departure_time == expectedStopTime.departure_time
        stopTimeList.first().stop_sequence == expectedStopTime.stop_sequence
        stopTimeList.first().stop_headsign == expectedStopTime.stop_headsign
        stopTimeList.first().pickup_type == expectedStopTime.pickup_type
        stopTimeList.first().drop_off_type == expectedStopTime.drop_off_type
        stopTimeList.first().shape_dist_traveled == expectedStopTime.shape_dist_traveled

    }

}
