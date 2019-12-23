package com.api.cms;

import com.framework.domain.cms.request.QueryPageRequest;
import com.framework.model.response.QueryResponseResult;

public interface CmsPageControllerApi {

    //页面查询
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest);
}
