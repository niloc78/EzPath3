package com.example.ezpath3.retrofit

import com.example.ezpath3.model.ErrandResults
import com.example.ezpath3.util.EntityMapper
import javax.inject.Inject

class NetworkErrandResultsMapper
@Inject
constructor() : EntityMapper<NetworkErrandResults, ErrandResults>
{


    //properties
//    var html_attributions : ArrayList<Any>,
//    var results : ArrayList<Result>,
//    var status : String
    override fun mapFromEntity(entity: NetworkErrandResults): ErrandResults {
        return ErrandResults(entity.html_attributions, entity.results, entity.status)
    }

    override fun mapToEntity(domainModel: ErrandResults): NetworkErrandResults {
        return NetworkErrandResults(domainModel.html_attributions, domainModel.results, domainModel.status)
    }


}