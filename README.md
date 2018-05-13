# Poynt Assignment

1. Directory structure
- albert_poynt : Main android code that creates Poynt payment fragment after receiving payment amount and currency from client.
- client_pos_app : a simple mockup of a merchant client build with android that sends the payment ammount and currency (USD by default)

2. Poynt Android application (albert_poynt)
- Receives a payment amount and currency from merchant's point of sale client
- create payment fragment for payment and send back the payment object to merchant's point of sale client
- important functions :
  - setSocketListener: sets listener to socket from firebase realtime database
  - updateFirebasePayResult: updates the payment results and send the payment data back to merchant client after the payment fragment has concluded
  - launchPayment: launched payment fragment after application receives ammount and currency from merchants client
  - onActivityResult: result returns after payment fragment concludes

3. merchant's point of sale client
- sends a simple payment request that includes an amount and currency(USD default) in the request to albert_poynt application
- receives incoming calls from poynt android app and displays payment object and success or failure of payment
- important functions :
  - updateFirebasePay: sends amount and currency to poynt application to create payment fragment
  - setSocketListener: sets listener to socket from firebase to receive the results of the payment fragment

4. Security, connectivity considerations or assumptions
- assumptions: This application is based on the assumption that both client and poynt device will be online consistently. Another assumption is that all transaction sent will be received .
- Security : currently for testing purposes I have not encrypted the json data separately but mainly depending on firebase to handle any security issues. A fix if I had more time would be to add my own encryptions such as symmetric encryption or certificates. Also I would add an authentication method for merchant client with firebase auth or other sources to generate a UUID.

- connectivity considerations : Since all communication is solely depending on wifi I would use a queue structure on the Poynt app and client side in case internet connectivity is down or has other issues. I would implement a persistent model to store unsent data for a short period in a queue and have the option to delete the transaction if it did not go through.

5. Unit and InstrumentedTest:

- SocketInstrumentedTest: espresso tests for testing ui and functions with context

- SocketUnitTest:  Tests MainActivity and context
