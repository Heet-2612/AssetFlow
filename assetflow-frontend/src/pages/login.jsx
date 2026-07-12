import { useState } from "react";
import axios from "axios";

function Login(){

const [email,setEmail]=useState("");
const [password,setPassword]=useState("");
const [message,setMessage]=useState("");

const handleLogin=async(e)=>{
    e.preventDefault();

    try {

        const response = await axios.post(
            "http://localhost:8080/api/auth/login",
            {
                email,
                password
            }
        );

        console.log(response.data);

        setMessage("Login Successful");

        localStorage.setItem(
            "token",
            response.data.token
        );

    } catch(error){

        console.log(error);

        setMessage("Login Failed");

    }
}


return(

<div className="container mt-5">

<div className="row justify-content-center">

<div className="col-md-4">

<div className="card shadow p-4">

<h2 className="text-center mb-4">
AssetFlow
</h2>


<form onSubmit={handleLogin}>


<input
className="form-control mb-3"
placeholder="Email"
value={email}
onChange={(e)=>setEmail(e.target.value)}
/>


<input
className="form-control mb-3"
type="password"
placeholder="Password"
value={password}
onChange={(e)=>setPassword(e.target.value)}
/>


<button className="btn btn-primary w-100">
Login
</button>


</form>

<p className="mt-3 text-center">
{message}
</p>


</div>

</div>

</div>

</div>

)

}

export default Login;