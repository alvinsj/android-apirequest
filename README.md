## android-apirequest
<b>android-apirequest</b> is an Android Library Project to handle API request.
 
### Getting Started
Start an API Request

```
new APIRequest( new APIManager(HomeActivity.this), "http://[server-url]/search", APIRequest.GET)
            .withDefaultParams(defaultParamsHashMap())
            .addParam("format", "json")
            .addParam("q", searchTerm)
            .addHeaderParam("Referer", APP_REFERER);
            .start(new JSONRequestHandler(){

                    @Override
                    public void onException(Exception e) {
                        e.printStackTrace();
                    }
                    
                    @Override
                    public void onResponse(HttpEntity response) {
                      super.onResponse(response);
                      JSONObject obj = getResponse();
                    }
            });
```

### Related Projects
- [android-cache](http://github.com/alvinsj/android-cache)
- [android-apirequest](http://github.com/alvinsj/android-apirequest)
