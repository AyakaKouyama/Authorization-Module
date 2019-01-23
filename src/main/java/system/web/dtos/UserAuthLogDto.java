package system.web.dtos;

import com.andrew.modelmapper.annotations.MmConvertedValue;
import com.andrew.modelmapper.annotations.MmValue;
import com.andrew.modelmapper.core.LocalDateTimeConverter;

import javax.json.bind.annotation.JsonbNillable;
import javax.json.bind.annotation.JsonbPropertyOrder;

@JsonbNillable
@JsonbPropertyOrder({"id", "login", "generation_date", "description"})
public class UserAuthLogDto {

    @MmValue(fieldName = "id")
    private Long id;

    @MmValue(fieldName = "login")
    private String login;

    @MmConvertedValue(fieldName = "generationDate", convertertClass = LocalDateTimeConverter.class)
    private String generationDate;

    @MmValue(fieldName = "description")
    private String description;

    public UserAuthLogDto() {
        //default public constructor
    }

    public Long getId(){
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getGenerationDate() {
        return generationDate;
    }

    public void setGenerationDate(String generationDate) {
        this.generationDate = generationDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
