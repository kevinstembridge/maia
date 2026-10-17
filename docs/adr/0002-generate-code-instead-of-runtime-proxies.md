# Generate source code instead of using runtime proxies

Maia generates source for every Layer, even though Spring Boot advertises "absolutely no code generation". Generated code is visible: unlike behaviour produced by runtime dynamic proxies, it can be read to understand what it does and stepped through in a debugger.
