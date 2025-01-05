#!groovy

def decidePipeline(Map configMap){
    appication = configMap.get("application")
    switch(appication) {
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
           echo "No pipeline found for application ${appication}"
           echo "aplication is not recongnised"
        break
    }
}