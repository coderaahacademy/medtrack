package com.medtrack.service;
import com.medtrack.entity.Fulfillment; import com.medtrack.repository.FulfillmentRepository; import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class FulfillmentService {
    private final FulfillmentRepository fulfillmentRepository;
    public FulfillmentService(FulfillmentRepository fulfillmentRepository) { this.fulfillmentRepository = fulfillmentRepository; }
    public List<Fulfillment> getAllFulfillments() { return fulfillmentRepository.findAll(); }
    public Fulfillment createFulfillment(Fulfillment fulfillment) { return fulfillmentRepository.save(fulfillment); }
}

import com.medtrack.entity.Fulfillment;
import com.medtrack.repository.FulfillmentRepository;
import org.springframework.stereotype.Service;

@Service
public class FulfillmentService {

    private final FulfillmentRepository repository;


    public FulfillmentService(FulfillmentRepository repository) {
        this.repository = repository;
    }

    public Fulfillment saveFulfillment(Fulfillment fulfillment) {

        fulfillment.setStatus("PENDING");
        return repository.save(fulfillment);
    }
}
