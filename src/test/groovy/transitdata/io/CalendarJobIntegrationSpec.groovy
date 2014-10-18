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
import transitdata.io.domain.Calendar

@ContextConfiguration('/jobs/calendar-job.xml')
@TestExecutionListeners([
        DependencyInjectionTestExecutionListener,
        StepScopeTestExecutionListener
])
class CalendarJobIntegrationSpec extends Specification {

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
        Calendar expectedCalendar = new Calendar(
                service_id: 'AUG14-MVS-BUS-Weekday-01',
                monday: '1',
                tuesday: '0',
                wednesday: '1',
                thursday: '0',
                friday: '1',
                saturday: '0',
                sunday: '0',
                start_date: '20141008',
                end_date: '20141212',
        )

        when: 'job is run'
        def jobExecution = jobLauncher.launchJob()
        List<Calendar> calendarList = jdbcTemplate.queryForList('select * from calendar')

        then: 'it completes successfully (execution is synchronous, so we don\'t have to wait for completion)'
        jobExecution.exitStatus == ExitStatus.COMPLETED

        and:
        calendarList.size() == 22
        calendarList.first().service_id == expectedCalendar.service_id
        calendarList.first().end_date == expectedCalendar.end_date
    }
}
