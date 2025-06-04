import { useState } from 'react';
import '../FormCard.css';

function Register() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [email, setEmail] = useState("");
    const [phoneNumber, setPhoneNumber] = useState("");
    const [emailAdress, setEmailAdress] = useState("");
    const [error, setError] = useState("");

    async function handleRegister(e) {
        e.preventDefault();

        const res = await fetch("/api/users", {
            method: 'POST',
            credentials: 'include',
            headers: { 
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                email: email,
                username: username,
                password: password,
                contactInfo: {
                    phoneNumber: phoneNumber,
                    emailAdress: emailAdress
                }
            })
        });
        
        const data = await res.json();
        if (res.ok && data._id !== undefined) {
            localStorage.setItem('token', data.token);
            window.location.href = "/";
        } else {
            setUsername("");
            setPassword("");
            setEmail("");
            setPhoneNumber("");
            setEmailAdress("");
            setError(data.message || "Registration failed. Please try again.");
        }
    }

    return (
        <div className="form-card-container">
            <div className="form-card">
                <h2>Register</h2>
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
                        <label htmlFor="emailAdress" className="form-label">Contact Email</label>
                        <input 
                            type="email"
                            className="form-control"
                            id="emailAdress"
                            placeholder="Enter your contact email"
                            value={emailAdress}
                            onChange={(e) => setEmailAdress(e.target.value)}
                        />
                    </div>
                    <button type="submit" className="btn btn-primary w-100">Register</button>
                </form>
            </div>
        </div>
    );
}

export default Register;