package com.manager.animallist.payload;

import lombok.Data;

@Data
public class AnimalDetailsRequest {

    private String birthDate;
    private String gender;
    private String nickName;
   
}