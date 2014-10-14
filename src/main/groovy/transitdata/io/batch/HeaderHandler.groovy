package transitdata.io.batch

import groovy.util.logging.Slf4j
import org.springframework.batch.item.file.LineCallbackHandler
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.beans.factory.annotation.Autowired

@Slf4j
class HeaderHandler implements LineCallbackHandler {

    @Autowired
    DelimitedLineTokenizer lineTokenizer

    @Override
    void handleLine(String line) {
        lineTokenizer.names = line.split(',')
    }
}
