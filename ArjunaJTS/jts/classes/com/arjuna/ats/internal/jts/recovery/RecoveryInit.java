/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RecoveryInit.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.recovery;

import java.util.List;

import com.arjuna.ats.jts.logging.jtsLogger;
import com.arjuna.common.internal.util.ClassloadingUtility;
import com.arjuna.orbportability.ORBInfo;
import com.arjuna.orbportability.ORBType;
import com.arjuna.orbportability.common.opPropertyManager;

/**
 * Registers the appropriate classes with the ORB.
 *   An application using the Transaction Service should load an instance of this class
 *   prior to orb-initialisation.
 *   Loading an instance can be achieved by naming the class in an OrbPreInit
 *   property.
 *   <p>Orb-specific details of recovery are handled by this class.
 *
 * @author Malik SAHEB
 *
 */

public class RecoveryInit
{

    private static boolean _initialised = false;
    private static boolean _isNormalProcess = true;

    /**
     * Constructor does the work as a result of being registered as an ORBPreInit
     * class
     */
    public RecoveryInit ()
    {
	/*
	 * We only really want a single instance of this class to be executed,
	 * no matter how many ORBs we may have in a process.
	 */

	if (!_initialised)
	{
	    _initialised = true;

	    // the eventhandler is the same for all orbs (at the moment)
	    String eventHandlerClassName = "com.arjuna.ats.internal.jts.recovery.contact.RecoveryContactWriter";

	    if ( _isNormalProcess)
	    {
		try
		{
		    // Use Here a class that should be initialized with a specific class specific to the ORB
		    // To determine the class to load use the ORBType

		    int orbType = ORBInfo.getOrbEnumValue();
		    String initClassName;

		    switch (orbType)
		    {
		    case ORBType.JAVAIDL:
			initClassName = "com.arjuna.ats.internal.jts.orbspecific.javaidl.recoverycoordinators.JavaIdlRecoveryInit";
			break;
		    default: {
                initClassName = null;
                jtsLogger.i18NLogger.warn_recovery_recoveryinit_1();
            }
			break;
		    }

            if (initClassName != null)
            {
                Class recoveryCoordinatorInitialiser = ClassloadingUtility.loadClass(initClassName);

                recoveryCoordinatorInitialiser.newInstance();
            }

		    // register the ContactWriter to watch for the first ArjunaFactory construction

            List<String> eventHandlers = opPropertyManager.getOrbPortabilityEnvironmentBean().getEventHandlerClassNames();
            eventHandlers.add(eventHandlerClassName);
		    opPropertyManager.getOrbPortabilityEnvironmentBean().setEventHandlerClassNames(eventHandlers);

		    // Change here above the way to call this startRCService -
		    // otherwise call it in JacOrbRecoveryInit above.
		}
		catch (Exception e)
		{
            jtsLogger.i18NLogger.fatal_recovery_RecoveryInit_4();

		    throw new com.arjuna.ats.arjuna.exceptions.FatalError(e.toString(), e);
		}

		if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("added ORBAttribute for recoveryCoordinatorInitialiser");
            jtsLogger.logger.debug("added event handler "+eventHandlerClassName);
        }
	    }
	}
    }
	/**
	 * This static method is used by the RecoveryManager to suppress
	 * aspects of recovery enablement in it's own
	 * process, without requiring further property manipulations
	 */

    public static void isNotANormalProcess()
    {
	_isNormalProcess = false;
    }

    public static boolean isNormalProcess ()
    {
	return _isNormalProcess;
    }

}

