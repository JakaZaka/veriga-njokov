import { useState } from 'react';

function Register() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [email, setEmail] = useState("");
    const [phoneNumber, setPhoneNumber] = useState("");
    const [emailAddress, setEmailAddress] = useState("");
    const [error, setError] = useState("");

    async function handleRegister(e) {
        e.preventDefault();
        setError("");
        try {
            const res = await fetch("/api/users", {
                method: 'POST',
                credentials: 'include',
                headers: { 
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    email,
                    username,
                    password,
                    contactInfo: {
                        phoneNumber,
                        emailAddress
                    }
                })
            });
            let data;
            try {
                data = await res.json();
            } catch {
                data = {};
            }
            if (res.ok && data._id !== undefined) {
                localStorage.setItem('token', data.token);
                window.location.href = "/";
            } else {
                setError(data.message || "Registration failed. Please try again.");
            }
        } catch (err) {
            setError("Network or server error.");
        }
    }

    return (
        <div className="container d-flex justify-content-center align-items-center" style={{ minHeight: "80vh" }}>
            <div className="card shadow-lg p-4 rounded-4" style={{ width: "100%", maxWidth: "400px" }}>
                <h2 className="text-center mb-4">Register</h2>
                {error && (
                    <div className="alert alert-danger" role="alert">
                        {error}
                    </div>
                )}
                <form onSubmit={handleRegister}>
                    <div className="mb-3">
                        <label htmlFor="email" className="form-label">Email</label>
                        <input 
                            type="email" 
                            className="form-control" 
                            id="email" 
                            placeholder="Enter your email" 
                            value={email} 
                            onChange={(e) => setEmail(e.target.value)} 
                            required
                        />
                    </div>
                    <div className="mb-3">
                        <label htmlFor="username" className="form-label">Username</label>
                        <input 
                            type="text" 
                            className="form-control" 
                            id="username" 
                            placeholder="Choose a username" 
                            value={username} 
                            onChange={(e) => setUsername(e.target.value)} 
                            required
                        />
                    </div>
                    <div className="mb-3">
                        <label htmlFor="password" className="form-label">Password</label>
                        <input 
                            type="password" 
                            className="form-control" 
                            id="password" 
                            placeholder="Create a password" 
                            value={password} 
                            onChange={(e) => setPassword(e.target.value)} 
                            required
                        />
                    </div>
                    <div className="mb-3">
                        <label htmlFor="phoneNumber" className="form-label">Phone Number</label>
                        <input 
                            type="tel" 
                            className="form-control" 
                            id="phoneNumber" 
                            placeholder="Enter your phone number" 
                            value={phoneNumber} 
                            onChange={(e) => setPhoneNumber(e.target.value)} 
                        />
                    </div>
                    <div className="mb-3">
                        <label htmlFor="emailAddress" className="form-label">Contact Email</label>
                        <input 
                            type="email" 
                            className="form-control" 
                            id="emailAddress" 
                            placeholder="Enter your contact email" 
                            value={emailAddress} 
                            onChange={(e) => setEmailAddress(e.target.value)} 
                        />
                    </div>
                    <button type="submit" className="btn btn-primary w-100">Register</button>
                </form>
            </div>
        </div>
    );
}

export default Register;