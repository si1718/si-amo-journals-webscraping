package web.service;

import java.io.IOException;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;


@RestController
public class ServingLayerController {

	@RequestMapping("/scholar") //recurso que se va a obtener. Hay que pasarle los parametros que se necesitan (id. tag..)
	public Object serve(
			@RequestParam(value = "id", required = true, defaultValue = "null") String tag)
			throws JsonParseException, JsonMappingException, IOException {
		return null;
	//	return TestJsoupScholar.getImgInfoFromGoogleScholar(tag);
		
		// After Run as Java application go to something like: http://localhost:8080/scholar?id=O719x-wAAAAJ
	}
	


}