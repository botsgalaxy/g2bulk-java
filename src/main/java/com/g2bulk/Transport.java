package com.g2bulk;

import java.io.IOException;

public interface Transport {
    TransportResponse send(TransportRequest request) throws IOException;
}
