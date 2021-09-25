package com.example.ezpath3.retrofit

import com.example.ezpath3.model.Result

data class NetworkErrandResults(
    var html_attributions : ArrayList<Any>,
    var results : ArrayList<Result>,
    var status : String
)
