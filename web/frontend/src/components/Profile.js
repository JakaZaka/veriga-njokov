import { useContext, useEffect, useState, useRef } from 'react';
import { UserContext } from '../userContext';
import { Navigate } from 'react-router-dom';

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

    useEffect(function () {
        const getProfile = async function () {
            setError("");
            try {
                const token = localStorage.getItem('token');
                const res = await fetch("/api/users/profile", {
                    credentials: "include",
                    headers: token ? { Authorization: `Bearer ${token}` } : {},
                });
                
                if (!res.ok) {
                    throw new Error("Failed to fetch profile");
                }
                
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
                userContext.setUserContext(data);
            } catch (err) {
                setError("Could not load your profile. Please try again later.");
                console.error(err);
            } finally {
                setLoading(false);
            }
        }
        getProfile();
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

    return (
        <div className="container py-5">
            {loading ? (
                <div className="text-center my-5">
                    <div className="spinner-border text-primary" role="status">
                        <span className="visually-hidden">Loading...</span>
                    </div>
                </div>
            ) : (
                <div className="row">
                    {/* Profile sidebar with avatar */}
                    <div className="col-md-4 mb-4">
                        <div className="card shadow rounded-4">
                            <div className="card-body text-center">
                                <div className="position-relative d-inline-block mb-3">
                                    <img
                                        src={avatarPreview || "https://via.placeholder.com/150"}
                                        alt="User Avatar"
                                        className="rounded-circle img-thumbnail"
                                        style={{ width: "150px", height: "150px", objectFit: "cover" }}
                                        onClick={() => fileInputRef.current.click()}
                                    />
                                    <div className="position-absolute bottom-0 end-0">
                                        <button 
                                            type="button" 
                                            className="btn btn-sm btn-primary rounded-circle"
                                            onClick={() => fileInputRef.current.click()}
                                        >
                                            <i className="bi bi-camera-fill"></i>
                                        </button>
                                    </div>
                                </div>
                                <h3 className="mb-1">{profile.username}</h3>
                                <p className="text-muted">
                                    {profile.role === 'admin' ? (
                                        <span className="badge bg-danger">Administrator</span>
                                    ) : (
                                        <span className="badge bg-primary">User</span>
                                    )}
                                </p>
                                <form onSubmit={handleAvatarUpload} className="mt-3">
                                    <input
                                        type="file"
                                        accept="image/*"
                                        style={{ display: "none" }}
                                        ref={fileInputRef}
                                        onChange={handleAvatarChange}
                                    />
                                    {avatarFile && (
                                        <button type="submit" className="btn btn-sm btn-outline-success" disabled={saving}>
                                            {saving ? "Saving..." : "Save new picture"}
                                        </button>
                                    )}
                                </form>
                            </div>
                        </div>
                    </div>

                    {/* Profile content */}
                    <div className="col-md-8">
                        {error && (
                            <div className="alert alert-danger alert-dismissible fade show" role="alert">
                                {error}
                                <button type="button" className="btn-close" onClick={() => setError("")}></button>
                            </div>
                        )}
                        
                        {success && (
                            <div className="alert alert-success alert-dismissible fade show" role="alert">
                                {success}
                                <button type="button" className="btn-close" onClick={() => setSuccess("")}></button>
                            </div>
                        )}

                        <div className="card shadow rounded-4">
                            <div className="card-header bg-white d-flex justify-content-between align-items-center py-3">
                                <h4 className="mb-0">Profile Information</h4>
                                {!editing && (
                                    <button 
                                        className="btn btn-primary" 
                                        onClick={() => setEditing(true)}
                                    >
                                        <i className="bi bi-pencil-square me-1"></i>
                                        Edit
                                    </button>
                                )}
                            </div>
                            <div className="card-body">
                                {editing ? (
                                    <form onSubmit={handleEditSubmit}>
                                        <div className="row">
                                            <div className="col-md-6 mb-3">
                                                <label className="form-label">Username</label>
                                                <input
                                                    type="text"
                                                    className="form-control"
                                                    name="username"
                                                    value={form.username}
                                                    onChange={handleEditChange}
                                                    required
                                                />
                                            </div>
                                            <div className="col-md-6 mb-3">
                                                <label className="form-label">Email</label>
                                                <input
                                                    type="email"
                                                    className="form-control"
                                                    name="email"
                                                    value={form.email}
                                                    onChange={handleEditChange}
                                                    required
                                                />
                                            </div>
                                        </div>
                                        <div className="mb-3">
                                            <label className="form-label">Password</label>
                                            <input
                                                type="password"
                                                className="form-control"
                                                name="password"
                                                value={form.password}
                                                onChange={handleEditChange}
                                                placeholder="Leave blank to keep current password"
                                            />
                                            <small className="text-muted">Only fill this if you want to change your password</small>
                                        </div>
                                        <div className="mb-3">
                                            <label className="form-label">Address</label>
                                            <input
                                                type="text"
                                                className="form-control"
                                                name="address"
                                                value={form.address}
                                                onChange={handleEditChange}
                                                placeholder="Enter your address"
                                            />
                                        </div>
                                        <div className="row">
                                            <div className="col-md-6 mb-3">
                                                <label className="form-label">Phone Number</label>
                                                <input
                                                    type="tel"
                                                    className="form-control"
                                                    name="phoneNumber"
                                                    value={form.phoneNumber}
                                                    onChange={handleEditChange}
                                                />
                                            </div>
                                            <div className="col-md-6 mb-3">
                                                <label className="form-label">Contact Email</label>
                                                <input
                                                    type="email"
                                                    className="form-control"
                                                    name="emailAddress"
                                                    value={form.emailAddress}
                                                    onChange={handleEditChange}
                                                />
                                            </div>
                                        </div>
                                        <div className="d-flex gap-2">
                                            <button type="submit" className="btn btn-success" disabled={saving}>
                                                {saving ? "Saving..." : "Save Changes"}
                                            </button>
                                            <button 
                                                type="button" 
                                                className="btn btn-outline-secondary" 
                                                onClick={() => setEditing(false)}
                                            >
                                                Cancel
                                            </button>
                                        </div>
                                    </form>
                                ) : (
                                    <div className="row">
                                        <div className="col-md-6 mb-3">
                                            <h6>Account Info</h6>
                                            <dl className="row">
                                                <dt className="col-sm-4">Username</dt>
                                                <dd className="col-sm-8">{profile.username}</dd>
                                                <dt className="col-sm-4">Email</dt>
                                                <dd className="col-sm-8">{profile.email}</dd>
                                            </dl>
                                        </div>
                                        <div className="col-md-6 mb-3">
                                            <h6>Contact Info</h6>
                                            <dl className="row">
                                                <dt className="col-sm-4">Phone</dt>
                                                <dd className="col-sm-8">{profile.contactInfo?.phoneNumber || "-"}</dd>
                                                <dt className="col-sm-4">Contact Email</dt>
                                                <dd className="col-sm-8">{profile.contactInfo?.emailAddress || "-"}</dd>
                                                <dt className="col-sm-4">Address</dt>
                                                <dd className="col-sm-8">{profile.location?.address || "-"}</dd>
                                            </dl>
                                        </div>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default Profile;