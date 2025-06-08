import { useContext, useEffect, useState, useRef } from 'react';
import { UserContext } from '../userContext';
import { Navigate } from 'react-router-dom';
import '../ProfileCard.css';
import OutfitTrendChart from './OutfitTrendChart';
import DistrictSellingChart from './DistrictSellingChart';
import ClosetStats from './ClosetStats';    


function Profile() {
    const userContext = useContext(UserContext);
    const [profile, setProfile] = useState({});
    const [loading, setLoading] = useState(true);
    const [avatarFile, setAvatarFile] = useState(null);
    const [avatarPreview, setAvatarPreview] = useState(null);
    const [saving, setSaving] = useState(false);
    const [editing, setEditing] = useState(false);
    const [form, setForm] = useState({
        username: "",
        email: "",
        password: "",
        phoneNumber: "",
        emailAddress: "",
        address: "",
    });
    const fileInputRef = useRef();
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [chartData, setChartData] = useState([]);
    const [districtData, setDistrictData] = useState([]);
    const [closetData, setClosetData] = useState([]);
    const [requests, setRequests] = useState([]);

    useEffect(function () {
        const getProfile = async function () {
            const token = localStorage.getItem('token');
            const res = await fetch("/api/users/profile", {
                credentials: "include",
                headers: token ? { Authorization: `Bearer ${token}` } : {},
            });
            const data = await res.json();
            setProfile(data);
            setAvatarPreview(data.avatar || null);
            setForm({
                username: data.username || "",
                email: data.email || "",
                password: "",
                phoneNumber: data.contactInfo?.phoneNumber || "",
                emailAddress: data.contactInfo?.emailAddress || "",
                address: data.location?.address || "",
            });
            setLoading(false);
            userContext.setUserContext(data);
        }
        getProfile();
        // eslint-disable-next-line
    }, []);

     useEffect(() => {
    fetch('/api/outfits/trends')
        .then(res => res.json())
        .then(setChartData)
        .catch(err => console.error("Failed to fetch trends", err));
    }, []);

    useEffect(() => {
    fetch('/api/users/districtSales')
        .then(res => res.json())
        .then(setDistrictData)
        .catch(err => console.error("Failed to fetch trends", err));
    }, []);

    const fetchRequests = async () => {
    try {
        const res = await fetch('/api/users/requests');
        const data = await res.json();
        setRequests(data);
        console.log("Fetched requests:", data);
    } catch (err) {
        console.error("Failed to fetch requests", err);
    }
    };

    useEffect(() => {
        fetchRequests();
    }, []);

    useEffect(() => {
    fetch('/api/clothing/closetStats')
        .then(res => res.json())
        .then(setClosetData)
        .catch(err => console.error("Failed to fetch trends", err));
    }, []);

    if (!userContext.user) {
        return <Navigate replace to="/login" />;
    }

    const handleAvatarChange = (e) => {
        const file = e.target.files[0];
        if (!file) return;
        
        // Simple validation for file size (2MB max)
        if (file.size > 2 * 1024 * 1024) {
            setError("Image is too large. Maximum size is 2MB.");
            return;
        }
        
        setAvatarFile(file);
        const reader = new FileReader();
        reader.onloadend = () => {
            setAvatarPreview(reader.result);
        };
        reader.readAsDataURL(file);
    };

    const handleAvatarUpload = async (e) => {
        e.preventDefault();
        if (!avatarFile) return;
        
        setSaving(true);
        setError("");
        setSuccess("");
        
        try {
            const reader = new FileReader();
            reader.onloadend = async () => {
                const base64Avatar = reader.result;
                const token = localStorage.getItem('token');
                
                const res = await fetch("/api/users/profile", {
                    method: "PUT",
                    credentials: "include",
                    headers: {
                        "Content-Type": "application/json",
                        ...(token ? { Authorization: `Bearer ${token}` } : {})
                    },
                    body: JSON.stringify({ avatar: base64Avatar }),
                });
                
                if (!res.ok) {
                    const err = await res.json();
                    throw new Error(err.message || "Failed to update avatar");
                }
                
                const data = await res.json();
                setProfile(data);
                setAvatarPreview(data.avatar || null);
                userContext.setUserContext(data);
                setSuccess("Profile picture updated successfully!");
                setAvatarFile(null);
            };
            reader.readAsDataURL(avatarFile);
        } catch (err) {
            setError(err.message || "Failed to update profile picture");
        } finally {
            setSaving(false);
        }
    };

    const handleEditChange = (e) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));
    };

    const handleEditSubmit = async (e) => {
        e.preventDefault();
        setSaving(true);
        setError("");
        setSuccess("");
        
        try {
            const token = localStorage.getItem('token');
            const res = await fetch("/api/users/profile", {
                method: "PUT",
                credentials: "include",
                headers: {
                    "Content-Type": "application/json",
                    ...(token ? { Authorization: `Bearer ${token}` } : {})
                },
                body: JSON.stringify({
                    username: form.username,
                    email: form.email,
                    password: form.password ? form.password : undefined,
                    contactInfo: {
                        phoneNumber: form.phoneNumber,
                        emailAddress: form.emailAddress
                    },
                    address: form.address,
                }),
            });
            
            if (!res.ok) {
                const err = await res.json();
                throw new Error(err.message || "Failed to update profile");
            }
            
            const data = await res.json();
            setProfile(data);
            setForm({
                username: data.username || "",
                email: data.email || "",
                password: "",
                phoneNumber: data.contactInfo?.phoneNumber || "",
                emailAddress: data.contactInfo?.emailAddress || "",
                address: data.location?.address || "",
            });
            userContext.setUserContext(data);
            setEditing(false);
            setSuccess("Profile updated successfully!");
        } catch (err) {
            setError(err.message || "Failed to update profile");
        } finally {
            setSaving(false);
        }
    };

   
    const handleAcceptRequest = async (userId, clothingId) => {
    try {
        const res = await fetch(`/api/clothing/transfer/${clothingId}/${userId}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ newUserId: userId })
        });

        if (!res.ok) {
        const data = await res.json();
        throw new Error(data.message);
        }

        alert("Transfer successful!");
        fetchRequests(); 
    } catch (err) {
        console.error(err);
        alert("Transfer failed: " + err.message);
    }
    };

    return (
        <>
        <div className="profile-card-container">
            <div className="profile-card">
                <div className="text-center mb-4">
                    <img
                        src={avatarPreview || "https://via.placeholder.com/150"}
                        alt="User Avatar"
                        className="profile-avatar-preview"
                        onClick={() => fileInputRef.current.click()}
                    />
                    <form onSubmit={handleAvatarUpload}>
                        <input
                            type="file"
                            accept="image/*"
                            style={{ display: "none" }}
                            ref={fileInputRef}
                            onChange={handleAvatarChange}
                        />
                        <button type="submit" className="btn btn-secondary mt-2" disabled={saving || !avatarFile}>
                            {saving ? "Saving..." : "Save new profile picture"}
                        </button>
                    </form>
                </div>
                <h2 className="text-center mb-4">{profile.username}</h2>
                {editing ? (
                    <form onSubmit={handleEditSubmit}>
                        <label className="form-label">Username</label>
                        <input
                            type="text"
                            className="form-control"
                            name="username"
                            value={form.username}
                            onChange={handleEditChange}
                            required
                        />
                        <label className="form-label">Email</label>
                        <input
                            type="email"
                            className="form-control"
                            name="email"
                            value={form.email}
                            onChange={handleEditChange}
                            required
                        />
                        <label className="form-label">New Password</label>
                        <input
                            type="password"
                            className="form-control"
                            name="password"
                            value={form.password}
                            onChange={handleEditChange}
                            placeholder="Leave blank to keep current password"
                        />
                        <label className="form-label">Phone Number</label>
                        <input
                            type="tel"
                            className="form-control"
                            name="phoneNumber"
                            value={form.phoneNumber}
                            onChange={handleEditChange}
                        />
                        <label className="form-label">Contact Email</label>
                        <input
                            type="email"
                            className="form-control"
                            name="emailAddress"
                            value={form.emailAddress}
                            onChange={handleEditChange}
                        />
                        <label className="form-label">Address</label>
                        <input
                            type="text"
                            className="form-control"
                            name="address"
                            value={form.address}
                            onChange={handleEditChange}
                            placeholder="Enter your address"
                        />
                        <button type="submit" className="btn btn-primary" disabled={saving}>
                            {saving ? "Saving..." : "Save Changes"}
                        </button>
                        <button type="button" className="btn btn-secondary mt-2" onClick={() => setEditing(false)}>
                            Cancel
                        </button>
                    </form>
                ) : (
                    <ul className="list-group list-group-flush">
                        <li className="list-group-item d-flex justify-content-between align-items-center">
                            <strong>Email:</strong>
                            <span>{profile.email}</span>
                        </li>
                        <li className="list-group-item d-flex justify-content-between align-items-center">
                            <strong>Phone Number:</strong>
                            <span>{profile.contactInfo?.phoneNumber || "-"}</span>
                        </li>
                        <li className="list-group-item d-flex justify-content-between align-items-center">
                            <strong>Contact Email:</strong>
                            <span>{profile.contactInfo?.emailAddress || "-"}</span>
                        </li>
                        <li className="list-group-item d-flex justify-content-between align-items-center">
                            <strong>Address:</strong>
                            <span>{profile.location?.address || "-"}</span>
                        </li>
                    </ul>
                )}
                {!editing && (
                    <button className="btn btn-outline-primary mt-3" onClick={() => setEditing(true)}>
                        Edit Profile
                    </button>
                )}
            </div>
        </div>
        <div className="requests-container">
        <h3>Requests</h3>
        {requests.length > 0 && requests.some(item => item.wantToGet.length > 0) ? (
            <div className="request-cards">
            {requests.map((item) =>
                item.wantToGet.map((user) => (
                <div
                    key={`${item.itemId}-${user._id}`}
                    className="card mb-3 p-3 shadow-sm"
                    style={{ borderRadius: "12px" }}
                >
                    <div className="d-flex justify-content-between align-items-center">
                    <div>
                        <strong>{user.username}</strong> wants to buy <strong>{item.name}</strong>
                    </div>
                    <button className="btn btn-success btn-sm"
                    onClick={() => {
                    handleAcceptRequest(user._id, item.itemId);
                    console.log(`Accepted request from ${user.username} for item ${item.name}`)
                    }}>
                        Accept
                    </button>
                    </div>
                </div>
                ))
            )}
            </div>
        ) : (
            <p>No requests found.</p>
        )}
        </div>
        <div className='graphs-container'>
            <div className='graphs'>
                <h3>Graphs and Statistics</h3>
                <OutfitTrendChart data={chartData} />
                <DistrictSellingChart data={districtData}/>
                <ClosetStats data={closetData}/>
            </div>
        </div>
    </>
    );
}

export default Profile;