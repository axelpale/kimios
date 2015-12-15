/*
 * Kimios - Document Management System Software
 * Copyright (C) 2008-2015  DevLib'
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * aong with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kimios.webservices.share;

import io.swagger.annotations.*;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.kimios.kernel.share.model.MailContact;
import org.kimios.webservices.exceptions.DMServiceException;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/**
 * Created by farf on 19/07/15.
 */
@WebService(targetNamespace = "http://kimios.org", serviceName = "ShareService")
@CrossOriginResourceSharing(allowAllOrigins = true)
@Api(value="/share", description = "Share Operations")
public interface ShareService {

    /**
     * Share provided documents by email    (SOAP Function)
     */
    void shareByEmail(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "documentIds") List<Long> documentIds,
            @WebParam(name = "recipients") Map<String, String> recipients,
            @WebParam(name = "subject") String subject,
            @WebParam(name = "content") String content,
            @WebParam(name = "senderAddress") String senderAddress,
            @WebParam(name = "senderName") String senderName,
            @WebParam(name = "defaultSender")  Boolean defaultSender)
            throws DMServiceException;




    /**
     * Share provided documents by email    (REST Method)
     */
    @POST
    @Path("/share-by-mail")
    @Consumes("application/json")
    @ApiOperation(value = "Share provided documents by email",
            notes = "Share documents by email. An exception will be thrown if the total size exceed the parameters",
            response = void.class)
    void shareByEmailFullContact(
            @ApiParam(value = "sessionId")
            @QueryParam(value = "sessionId") String sessionId,

            @ApiParam(value = "documentIds")
            @QueryParam(value = "documentIds") List<Long> documentIds,


            @ApiParam(value = "recipients")
            List<MailContact> recipients,

            @ApiParam(value = "subject")
            @QueryParam(value = "subject")  String subject,

            @ApiParam(value = "content")
            @QueryParam(value = "content")  String content,

            @ApiParam(value = "senderAddress")
            @QueryParam(value = "senderAddress")  String senderAddress,

            @ApiParam(value = "senderName")
            @QueryParam(value = "senderName") String senderName,


            @DefaultValue("false") @ApiParam(value = "Default Sender")  @QueryParam(value = "defaultSender")  Boolean defaultSender)
            throws DMServiceException;


    @GET
    @Path("/search-contact")
    @ApiOperation(value = "List Contact",
            notes = "Provide list of previously used contacts",
            response = MailContact.class, responseContainer = "List")
    List<MailContact> searchContact(

            @ApiParam(value = "sessionId", name = "sessionId", required = true)
            @QueryParam(value = "sessionId") @WebParam(name = "sessionId") String sessionId,

            @ApiParam(value = "query", name = "query", required = true)
            @QueryParam(value = "query") @WebParam(name = "query") String query
            )
        throws DMServiceException;
}
