package burrow.kernel.stream

import java.io.InputStream

class StateInputStream(
    private val controller: StateInputStreamController,
    private val inputStream: InputStream,
    private val state: String,
) {

}