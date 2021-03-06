/**
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.developers.msa.api_gateway;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.ServerSpan;
import com.redhat.developers.msa.api_gateway.feign.FeignClientFactory;

import io.swagger.annotations.ApiOperation;

@RestController
public class ApiGatewayController {

    @Autowired
    private FeignClientFactory feignClientFactory;

    @Autowired
    private Brave brave;

    /**
     * This /api REST endpoint uses Java 8 parallel stream to create the Feign, invoke it, and collect the result as a List that
     * will be rendered as a JSON Array.
     *
     * @return
     */
    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, value = "/api", produces = "application/json")
    @ApiOperation("Invoke all microservices in parallel")
    public List<String> api() {
        // This stores the Original/Parent ServerSpan from ZiPkin.
        ServerSpan serverSpan = brave.serverSpanThreadBinder().getCurrentServerSpan();
        return feignClientFactory.getFeignClients()
            .stream()
            .parallel()
            // We set the ServerSpan to each client to avoid loosing the tracking in a multi-thread invocation
            .map((feign) -> feign.invokeService(serverSpan))
            .collect(Collectors.toList());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/health")
    @ApiOperation("Used to verify the health of the service")
    public String health() {
        return "I'm ok";
    }
}
