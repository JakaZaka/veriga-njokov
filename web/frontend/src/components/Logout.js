import { useEffect, useContext } from 'react';
import { UserContext } from '../userContext';
import { Navigate } from 'react-router-dom';
import socket from '../socket'

function Logout() {
    const userContext = useContext(UserContext); 
    useEffect(function() {
        if (userContext.user?._id) {
            socket.emit('logout', userContext.user._id);
            // Optionally, disconnect socket if you want to fully close connection
            // socket.disconnect();
        }
        userContext.setUserContext(null);
        localStorage.removeItem('token');
    }, []);

    return (
        <Navigate replace to="/" />
    );
}

export default Logout;