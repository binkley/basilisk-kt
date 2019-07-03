package x.micronaut

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post

@Controller("/hello")
class HelloController {
    @Post
    fun greeting(request: HelloRequest) = HelloResponse("Hello, ${request.name}!")
}
