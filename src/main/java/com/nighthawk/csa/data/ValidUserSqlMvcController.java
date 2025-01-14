package com.nighthawk.csa.data;

import com.nighthawk.csa.data.SQL.Schedule;
import com.nighthawk.csa.data.SQL.User;
import com.nighthawk.csa.data.SQL.UserSqlRepository;
import com.nighthawk.csa.data.SQL.ScheduleSqlRepository;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.HashMap;
import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Built using article: https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/mvc.html
// or similar: https://asbnotebook.com/2020/04/11/spring-boot-thymeleaf-form-validation-example/
@Controller
public class ValidUserSqlMvcController implements WebMvcConfigurer {

    // Autowired enables Control to connect HTML and POJO Object to Database easily for CRUD
    @Autowired
    private UserSqlRepository repository;

    @Autowired
    private ScheduleSqlRepository scheduleRepository;

    @GetMapping("/userlist")
    public String user(Model model) {
        List<User> list = repository.listAll();
        model.addAttribute("list", list);
        return "user/userlist";
    }


    // CRUD OPERATIONS

    // Create
    @PostMapping("/data/usercreate")
    public String userSave(@Valid User user, BindingResult bindingResult) {
        // Validation of Decorated PersonForm attributes
        if (bindingResult.hasErrors()) {
            return "user/signup";
        }
        repository.save(user);
        // Redirect to next step
        return "redirect:/login";
    }

    // Read
    @GetMapping("/profile/process")
    public String processProfile(@RequestParam(name = "username", required = false, defaultValue = "default") String username, Model model) {


        try {
            List list = repository.listLike(username);
            User user = (User) list.get(0);
            return "redirect:/profile/" + user.id();
        }
        catch(Exception e) {
            List list = repository.listLike("defaultUser");
            User user = (User) list.get(0);
            return "redirect:/profile/" + user.id();
        }
        // model.addAttribute("user", user);

        // DEBUGGING BY SARAH

    }


    // update
    @PostMapping("/data/userupdate")
    public String personUpdateSave(@Valid User user, BindingResult bindingResult) {
        // Validation of Decorated PersonForm attributes
        if (bindingResult.hasErrors()) {
            return "data/userupdate";
        }
        repository.save(user);
        // Redirect to next step
        return "redirect:/profile/tutors";
    }

    // delete
    // Make this part admin only
    // after figuring out how to lock things behind login
    @GetMapping("/data/userdelete/{id}")
    public String personDelete(@PathVariable("id") long id) {
        repository.delete(id);
        return "redirect:/userlist";
    }


    // CRUD OPERATIONS END

    @GetMapping("/signup")
    public String userAdd(User user) {
        return "user/signup";
    }




    /*  The HTML template Forms and PersonForm attributes are bound
        @return - template for person form
        @param - Person Class
    */


    /* Gathers the attributes filled out in the form, tests for and retrieves validation error
    @param - Person object with @Valid
    @param - BindingResult object
     */


    @GetMapping("/login")
    public String signin(User user) {

        // DEBUGGING BY SARAH
        // model.addAttribute("user", repository.get(user.id()));
        return "user/login";
    }







    @GetMapping("/data/userupdate/{id}")
    public String personUpdate(@PathVariable("id") int id, Model model) {
        model.addAttribute("person", repository.get(id));
        return "data/userupdate";
    }



    // CRUD OPERATIONS END





    /*  The HTML template Forms and PersonForm attributes are bound
        @return - template for person form
        @param - Person Class
    */


    /* Gathers the attributes filled out in the form, tests for and retrieves validation error
    @param - Person object with @Valid
    @param - BindingResult object
     */



    /*
    @GetMapping("/data/userupdate/{id}")
    public String personUpdate(@PathVariable("id") int id, Model model) {
        model.addAttribute("person", repository.get(id));
        return "data/userupdate";
    }*/


    /*
    #### RESTful API section ####
    Resource: https://spring.io/guides/gs/rest-service/
    */

    /*
    GET List of People
     */
    @RequestMapping(value = "/api/users/get")
    public ResponseEntity<List<User>> getUsers() {
        return new ResponseEntity<>( repository.listAll(), HttpStatus.OK);
    }

    /*
    GET individual Person using ID
     */
    @RequestMapping(value = "/api/user/get/{id}")
    public ResponseEntity<User> getUser(@PathVariable long id) {
        return new ResponseEntity<>( repository.get(id), HttpStatus.OK);
    }

    /*
    DELETE individual Person using ID
     */
    @RequestMapping(value = "/api/user/delete/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Object> deleteUser(@PathVariable long id) {
        repository.delete(id);
        return new ResponseEntity<>( ""+ id +" deleted", HttpStatus.OK);
    }


    /*
    POST Aa record by Requesting Parameters from URI
     */
    @RequestMapping(value = "/api/user/post", method = RequestMethod.POST)
    public ResponseEntity<Object> postUser(@RequestParam("id") Long id,
                                            @RequestParam("username") String username,
                                             @RequestParam("name") String name,
                                             @RequestParam("dob") String dobString,
                                             @RequestParam("type") String type,
                                             @RequestParam("bio") String bio,
                                             @RequestParam("password") String password) {
        Date dob;
        try {
            dob = new SimpleDateFormat("MM-dd-yyyy").parse(dobString);
        } catch (Exception e) {
            return new ResponseEntity<>(dobString +" error; try MM-dd-yyyy", HttpStatus.BAD_REQUEST);
        }
        // A person object WITHOUT ID will create a new record
        User user = new User(id, username, name, dob, type, bio, password);
        repository.save(user);
        return new ResponseEntity<>(username +" is created successfully", HttpStatus.CREATED);
    }

    @GetMapping("/data/user_search")
    public String user() {
        return "data/user_search";
    }

    @GetMapping("/profile/{id}")
    public String profile(@PathVariable long id, Model model) {
        User user = repository.get(id);
        model.addAttribute("user", user);

        List<Schedule> scheduleList = scheduleRepository.listAll();
        model.addAttribute("list", scheduleList);
        return "user/profileUser";
    }

    /*
    The personSearch API looks across database for partial match to term (k,v) passed by RequestEntity body
     */
    @RequestMapping(value = "/api/user_search", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> userSearch(RequestEntity<Object> request) {

        // extract term from RequestEntity
        JSONObject json = new JSONObject((Map) Objects.requireNonNull(request.getBody()));
        String term = (String) json.get("term");

        // custom JPA query to filter on term
        List<User> list = repository.listLikeNative(term);

        // return resulting list and status, error checking should be added
        return new ResponseEntity<>(list, HttpStatus.OK);
    }


    // Hashmaps tests
    // Import the HashMap class

    @GetMapping("/hashmap")
    public String hashmap() {
        // Create a HashMap object of users
        // Testing this out - TODO: change to use for loop to create full user list hashmap
        HashMap<Integer, User> capitalCities = new HashMap<Integer, User>();

        // Add keys and values (Country, City)
        capitalCities.put(1, repository.get(1));
        capitalCities.put(2, repository.get(2));
        capitalCities.put(3, repository.get(3));
        capitalCities.put(4, repository.get(4));

        // prints it out
        System.out.println(capitalCities);

        // redirects to the userlist page
        return "redirect:/userlist";
    }


}