#!groovy

def decidePipeline(Map configMap){
    application = configMap.get("application")
    switch(application) {
        case 'nodejsVM':
            nodejsVM(configMap)
        break
        case 'javaVM':
            javaVM(configMap)
        break
        case 'nodejsEKS':
            nodejsEKS(configMap)
        break
        default:
           echo "No pipeline found for application ${application}"
           echo "aplication is not recongnised"
        break
    }
}