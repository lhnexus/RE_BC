// Disclaimer
//
// THIS SAMPLE CODE MAY BE USED SOLELY AS PART OF THE TEST AND EVALUATION OF THE SAP
// CLOUD PLATFORM BLOCKCHAIN SERVICE (THE “SERVICE”) AND IN ACCORDANCE WITH THE
// TERMS OF THE TEST AND EVALUATION AGREEMENT FOR THE SERVICE. THIS SAMPLE CODE
// PROVIDED “AS IS”, WITHOUT ANY WARRANTY, ESCROW, TRAINING, MAINTENANCE, OR
// SERVICE OBLIGATIONS WHATSOEVER ON THE PART OF SAP.
package main

import (
	"fmt"
	"encoding/json"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/hyperledger/fabric/protos/peer"
	"strings"
	"time"
	"strconv"
	"math/rand"
	"bytes"
)

// Main function starts up the chaincode in the container during instantiate
//
type Re struct {}

func main() {
	if err := shim.Start(new(Re)); err != nil {
		fmt.Printf("Main: Error starting House chaincode: %s", err)
	}
}

// Init is called during Instantiate transaction after the chaincode container
// has been established for the first time, allowing the chaincode to
// initialize its internal data. Note that chaincode upgrade also calls this
// function to reset or to migrate data, so be careful to avoid a scenario
// where you inadvertently clobber your ledger's data!
//

func (t *Re) Init(stub shim.ChaincodeStubInterface) peer.Response {
	// Validate supplied init parameters, in this case zero arguments!
	if _, args := stub.GetFunctionAndParameters(); len(args) > 0 {
		return shim.Error("Init: Incorrect number of arguments; none expected.")
	}
	return shim.Success(nil)
}

// Invoke is called to update or query the ledger in a proposal transaction.
// Updated state variables are not committed to the ledger until the
// transaction is committed.
//
func (cc *Re) Invoke(stub shim.ChaincodeStubInterface) peer.Response {
	// Which function is been called?
	function, args := stub.GetFunctionAndParameters()
	function = strings.ToLower(function)
	// Route call to the correct function
	switch function {
	case "register": return cc.register(stub, args)
	case "order": return cc.order(stub, args);
	case "transfer": return cc.transfer(stub, args);
	case "search": return cc.search(stub, args);
	default: return shim.Error("Valid methods are 'register' or 'search'!")
	}
}

// Write an ID and string to the blockchain
//
type house struct {
	Uuid string `json:"UUID"`
	License string `json:"LICENSE"`
	Address string `json:"ADDRESS"`
	Owner string `json:"OWNER"`
	Status int `json:"STATUS"`
	Effect_date time.Time `json:"EFFECT_DATE"`
	Completion_date	time.Time `json:"COMPLETION_DATE"`
	Developer string `json:"DEVELOPER"`
	Area float64 `json:"AREA"`
	Buyer string `json:"BUYER"`
	Valid_to time.Time `json:"VALID_TO"`
	Last_update time.Time `json:"LAST_UPDATE"`
}

type rg_return_message struct {
	Uuid string `json:"UUID"`
	License string `json:"LICENSE"`
}

type or_return_message struct {
	Uuid string `json:"UUID"`
}

func (cc *Re) register(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	//extract the ID and value from the arguments the content and object id
	if len(args) != 5 {
		return shim.Error("Register: incorrect arguments; expecting UUID & ADDRESS & DEVELOPER & COMPLETION_DATE & AREA.")
	}
	uuid := args[0]
	address := args[1]
	developer := args[2]
	completion_date, _ := time.Parse("01/02/2006", args[3])
	area, _ :=strconv.ParseFloat(args[4],64)


	//Validate that this UUID format meets standard
	if luuid := strings.Count(uuid,"");luuid != 33 {
		return shim.Error("Register: the length of this UUID must be 32.")
}
	//Validate that this ADDRESS meets standard
	if laddress :=strings.Count(strings.Trim(address, " "),"");laddress<=1 {
		return shim.Error("Register: ADDRESS is required.")
	}

	// Validate that this UUID does not yet exist
	messageAsBytes, err := stub.GetState(uuid)
	if err != nil {
		return shim.Error("Order: Failed to get house:" + err.Error())
	} else if messageAsBytes != nil {
		return shim.Error("Register: this UUID already has a house assigned.")
	}


	// Validate that this ADDRESS does not yet exist
	ad_messageAsBytes, ad_err := stub.GetStateByPartialCompositeKey("address",[]string{address})
	if ad_err != nil {
		return shim.Error("Order: Failed to get house:" + ad_err.Error())
	} else if ad_messageAsBytes.HasNext() {
		return shim.Error("Register: this ADDRESS already has a house assigned.")
	}

	// Write the message
	current_time := time.Now()
	valid_to := current_time.AddDate(70,0,0)
	license := generateLicense()
	msg := &house{Uuid: uuid, License: license, Address:address,  Owner: developer, Status: 1, Effect_date: current_time, Completion_date: completion_date, Developer: developer,  Area: area, Buyer: "", Valid_to: valid_to}
	msgJSON, _ := json.Marshal(msg)


	rt_msg := &rg_return_message{Uuid: uuid, License: license}
	payload,_ := json.Marshal(rt_msg)

	err = stub.PutState(uuid,msgJSON)
	if err != nil {
		return shim.Error(err.Error())
	}

	key,_ := stub.CreateCompositeKey("address", []string{address})
	value := []byte{0x00}
	stub.PutState(key, value)
	return shim.Success(payload)

}

func generateLicense() string{
	const  letterBytes  = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
	b := make([]byte, 6)
	for i := range b {
		b[i] = letterBytes[rand.Intn(len(letterBytes ))]
	}
	license :=  "沪房地字第"+string(b)+"号"
	return license
}

func (cc *Re) order(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	//extract the ID and value from the arguments the content and object id
	if len(args) != 2 {
		return shim.Error("Order: incorrect arguments; expecting UUID & BUYER.")
	}
	uuid := args[0]
	buyer := args[1]

	// Validate that this UUID exists

	messageAsBytes, err := stub.GetState(uuid)
	if err != nil {
		return shim.Error("Order: Failed to get house:" + err.Error())
	} else if messageAsBytes == nil {
		return shim.Error("Order: this UUID does not exist")
	}

	// Validate that the STATUS != 2
	checkresult := house{}
	err = json.Unmarshal(messageAsBytes, &checkresult) //unmarshal it aka JSON.parse()
	if err != nil {
		return shim.Error(err.Error())
	}
	if status := checkresult.Status; status == 2  {
		return shim.Error("Order: this house is locked.")
	}

	// Write the message

	checkresult.Buyer = buyer
	checkresult.Status = 2
	checkresult.Last_update = time.Now()

	updatedJSONasBytes, _ := json.Marshal(checkresult)

	err = stub.PutState(uuid, updatedJSONasBytes)
	if err != nil {
		return shim.Error(err.Error())
	}

	rt_msg := &or_return_message{Uuid: uuid}
	payload,_ := json.Marshal(rt_msg)
	return shim.Success(payload)

}


func (cc *Re) transfer(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	//extract the ID and value from the arguments the content and object id
	if len(args) != 2 {
		return shim.Error("Transfer: incorrect arguments; expecting UUID & BUYER.")
	}
	uuid := args[0]
	buyer := args[1]

	// Validate that this UUID exists

	messageAsBytes, err := stub.GetState(uuid)
	if err != nil {
		return shim.Error("Transfer: Failed to get house:" + err.Error())
	} else if messageAsBytes == nil {
		return shim.Error("Transfer: this UUID does not exist")
	}

	// Validate that the STATUS = 2
	checkresult := house{}
	err = json.Unmarshal(messageAsBytes, &checkresult) //unmarshal it aka JSON.parse()
	if err != nil {
		return shim.Error(err.Error())
	}
	if status := checkresult.Status; status != 2  {
		return shim.Error("Transfer: this house is not locked, cannot be transferred .")
	}

	if or_buyer := checkresult.Buyer; or_buyer != buyer{
		return shim.Error("Transfer: this buyer does not match the order buyer.")
	}

	// Write the message

	checkresult.Owner = buyer
	checkresult.Status = 3
	checkresult.Last_update = time.Now()

	updatedJSONasBytes, _ := json.Marshal(checkresult)

	err = stub.PutState(uuid, updatedJSONasBytes)
	if err != nil {
		return shim.Error(err.Error())
	}

	rt_msg := &or_return_message{Uuid: uuid}
	payload,_ := json.Marshal(rt_msg)
	return shim.Success(payload)

}

// Read a string from the blockchain, given its ID
//
func (cc *Re) search(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 2 {
		return shim.Error("Search: incorrect number of arguments; expecting UUID and operation.")
	}
	uuid := args[0]
	operation := strings.ToLower(args[1])
	var queryString string

	if operation == "equal"{
		queryString = fmt.Sprintf("{\"selector\":{\"UUID\":\"%s\"}}", uuid)
	}else if operation == "begin"{
		queryString = fmt.Sprintf("{\"selector\":{\"UUID\":{\"$regex\":\"(^)%s\"}}}", uuid)
	}else if operation == "end"{
		queryString = fmt.Sprintf("{\"selector\":{\"UUID\":{\"$regex\":\"%s($)\"}}}", uuid)
	}else{
		return shim.Error("Search: invalid OPERATION TAG supplied.")
	}

	if resultsIterator, err := stub.GetQueryResult(queryString); err != nil || !resultsIterator.HasNext(){
		return shim.Error("Search: invalid Query supplied."+ queryString)
	} else {
		results,err:=getListResult(resultsIterator)
		if err!=nil{
			return shim.Error("Search: getListResult failed")
		}
		return shim.Success(results)
	}


}


func getListResult(resultsIterator shim.StateQueryIteratorInterface) ([]byte,error){

	defer resultsIterator.Close()
	// buffer is a JSON array containing QueryRecords
	var buffer bytes.Buffer
	buffer.WriteString("[")

	bArrayMemberAlreadyWritten := false
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return nil, err
		}
		// Add a comma before array members, suppress it for the first array member
		if bArrayMemberAlreadyWritten == true {
			buffer.WriteString(",")
		}
		buffer.WriteString("{\"Key\":")
		buffer.WriteString("\"")
		buffer.WriteString(queryResponse.Key)
		buffer.WriteString("\"")

		buffer.WriteString(", \"Record\":")
		// Record is a JSON object, so we write as-is
		buffer.WriteString(string(queryResponse.Value))
		buffer.WriteString("}")
		bArrayMemberAlreadyWritten = true
	}
	buffer.WriteString("]")
	fmt.Printf("queryResult:\n%s\n", buffer.String())
	return buffer.Bytes(), nil
}


