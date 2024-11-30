package burrow.kernel.stream

import java.io.Reader
import java.util.concurrent.atomic.AtomicReference

class StateInputStreamController {
    private val currentState = AtomicReference("")
    private val stateReaders = mutableMapOf<String, Reader>()
}