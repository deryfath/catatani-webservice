package io.iotera.emma.smarthome.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@RestController
@RequestMapping("/res")
public class ESResourceController extends ESBaseController {

    @RequestMapping(value = "/image/**", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity image(HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        path = path.substring("/res/image/".length());

        File file = new File(getProperty("attachment.path") + '/' + path);
        try {
            InputStream is = new FileInputStream(file);
            return ResponseEntity.ok().
                    contentLength(file.length()).
                    contentType(MediaType.IMAGE_PNG).
                    body(new InputStreamResource(is));

        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        }
        return notFound("");
    }

}
