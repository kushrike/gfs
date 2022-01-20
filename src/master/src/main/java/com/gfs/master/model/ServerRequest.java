package com.gfs.master.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * created by nikunjagarwal on 19-01-2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerRequest <T>{
    private Source source;
    private T request;
}